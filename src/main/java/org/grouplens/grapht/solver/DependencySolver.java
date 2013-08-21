/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.solver;

import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.AttributesImpl;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * DependencySolver is a utility for resolving Desires into a dependency graph,
 * where nodes are shared when permitted by a Satisfaction's dependency
 * configuration. It supports qualified and context-aware injection, and
 * just-in-time injection if the type has an injectable constructor.
 * <p>
 * The conceptual binding function used by this solver is represented as a list
 * of prioritized {@link BindingFunction functions}. Functions at the start of
 * the list are used first, which makes it easy to provide custom functions that
 * override default behaviors.
 * <p>
 * This solver does not support cyclic dependencies because of the possibility
 * that a context later on might activate a bind rule that breaks the cycle. To
 * ensure termination, it has a maximum context depth that is configurable.
 * 
 * @see DefaultInjector
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class DependencySolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencySolver.class);
    
    private final int maxDepth;
    private final CachePolicy defaultPolicy;

    private final List<BindingFunction> functions;
    
    private final Graph graph;
    private final Node root; // this has a null label
    
    private final Queue<DeferredResult> deferredNodes;

    /**
     * Create a DependencySolver that uses the given functions, and max
     * depth of the dependency graph.
     * 
     * @param bindFunctions The binding functions that control desire bindings
     * @param maxDepth A maximum depth of the graph before it's determined that
     *            a cycle exists
     * @throws IllegalArgumentException if maxDepth is less than 1
     * @throws NullPointerException if bindFunctions is null
     * @deprecated Use {@link DependencySolverBuilder} (and {@link #newBuilder()}) instead.
     */
    @Deprecated
    public DependencySolver(List<BindingFunction> bindFunctions, CachePolicy defaultPolicy, int maxDepth) {
        Preconditions.notNull("bindFunctions", bindFunctions);
        Preconditions.notNull("defaultPolicy", defaultPolicy);
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("Max depth must be at least 1");
        }
        
        this.functions = new ArrayList<BindingFunction>(bindFunctions);
        this.maxDepth = maxDepth;
        this.defaultPolicy = defaultPolicy;
        
        deferredNodes = new ArrayDeque<DeferredResult>();
        
        graph = new Graph();
        root = new Node();
        graph.addNode(root);

        logger.info("DependencySolver created, max depth: {}", maxDepth);
    }

    /**
     * Create a new dependency solver builder.
     *
     * @return A dependency solver builder.
     */
    public static DependencySolverBuilder newBuilder() {
        return new DependencySolverBuilder();
    }
    
    /**
     * @return The resolved dependency graph
     */
    public Graph getGraph() {
        return graph;
    }
    
    /**
     * @return The root node of the graph, with a null label
     */
    public Node getRootNode() {
        return root;
    }
    
    /**
     * Update the dependency graph to include the given desire. An edge from the
     * root node to the desire's resolved satisfaction will exist after this is
     * finished.
     * 
     * @param desire The desire to include in the graph
     */
    public void resolve(Desire desire) throws SolverException {
        logger.info("Resolving desire: {}", desire);
        
        try {
            // should not have any nodes waiting to be processed at the start
            assert deferredNodes.isEmpty();
            
            // before any deferred nodes are processed, we use a synthetic root
            // and null original desire since nothing produced this root
            InjectionContext initialContext =
                    InjectionContext.empty().push(null, new AttributesImpl());
            deferredNodes.add(new DeferredResult(new Node(), null, initialContext));
            
            while(!deferredNodes.isEmpty()) {
                DeferredResult treeRoot = deferredNodes.poll();
                
                Map<Node, DeferredResult> toResolve = new HashMap<Node, DeferredResult>();
                Graph tree = new Graph();
                tree.addNode(treeRoot.deferredNode);

                if (treeRoot.deferredNode.getLabel() == null) {
                    resolveFully(desire, treeRoot.deferredNode, tree, treeRoot.originalContext, toResolve);
                } else {
                    Satisfaction deferredSatisfaction = treeRoot.deferredNode.getLabel().getSatisfaction();
                    for (Desire d: deferredSatisfaction.getDependencies()) {
                        logger.debug("Attempting to resolve deferred dependency {} of {}", d, deferredSatisfaction);
                        InjectionContext newContext = treeRoot.originalContext.push(deferredSatisfaction, treeRoot.originalDesire.getInjectionPoint().getAttributes());
                        resolveFully(d, treeRoot.deferredNode, tree, newContext, toResolve);
                    }
                }
                merge(tree, treeRoot.deferredNode, toResolve);
            }
        } catch(SolverException e) {
            logger.error("Error while resolving: {}", e.getMessage());
            throw e;
        } catch(RuntimeException e) {
            logger.error("Error while resolving: {}", e.getMessage());
            throw e;
        }
    }
    
    private void merge(Graph fullTree, Node root, Map<Node, DeferredResult> defer) {
        List<Node> sorted = fullTree.sort(root);
        
        // Look up each node's dependencies in the merged graph, since we sorted
        // by reverse depth we can guarantee that dependencies have already
        // been merged
        Map<Node, Node> mergedMap = new HashMap<Node, Node>();
        for (Node toMerge: sorted) {
            if (toMerge == root && root.getLabel() == null) {
                // This is the synthetic root of the tree.
                // We replace the root node of the tree with the root in the merged graph.
                for (Edge oldEdge: fullTree.getOutgoingEdges(root)) {
                    Node newTail = mergedMap.get(oldEdge.getTail());
                    assert newTail != null; // like below, it must have been merged previously

                    // there can be at most one edge with this label in the merged
                    // graph because this is at the root context, and there is no
                    // way to cause their configurations to diverge
                    if (graph.getOutgoingEdge(this.root, oldEdge.getDesireChain()) ==  null) {
                        // this desire is not in the merged graph
                        graph.addEdge(new Edge(this.root, newTail, oldEdge.getDesireChain()));
                    }
                }
            } else {
                // Get all previously seen dependency configurations for this satisfaction
                Map<Set<Node>, Node> dependencyOptions = getDependencyOptions(toMerge.getLabel());
                
                // Accumulate the set of dependencies for this node, filtering
                // them through the previous level map
                Set<Node> dependencies = new HashSet<Node>();
                for (Edge dep: fullTree.getOutgoingEdges(toMerge)) {
                    // levelMap converts from the tree to the merged graph
                    Node filtered = mergedMap.get(dep.getTail());
                    assert filtered != null; // all dependencies should have been merged previously
                    dependencies.add(filtered);
                }
                
                Node newNode = dependencyOptions.get(dependencies);
                if (newNode == null) {
                    // this configuration for the satisfaction has not been seen before
                    // - add it to merged graph, and connect to its dependencies

                    if (toMerge == root) {
                        // non-cyclic deferred node from a prior resolution, so toMerge
                        // is already in this graph (but had no edges, so it didn't
                        // get identified correctly)
                        newNode = toMerge;
                        logger.debug("Linking deferred satisfaction to graph: {}", toMerge.getLabel());
                    } else {
                        // create a new node
                        newNode = new Node(toMerge.getLabel());
                        graph.addNode(newNode);
                        logger.debug("Adding new node to merged graph for satisfaction: {}", toMerge.getLabel());
                    }

                    for (Edge dep: fullTree.getOutgoingEdges(toMerge)) {
                        // add the edge with the new head and the previously merged tail
                        // List<Desire> is downsized to the first Desire, too
                        Node filtered = mergedMap.get(dep.getTail());
                        graph.addEdge(new Edge(newNode, filtered, dep.getDesireChain()));
                    }

                    // if the original node was in the defer queue, insert
                    // merged node into the final queue
                    if (defer.containsKey(toMerge)) {
                        DeferredResult oldResult = defer.get(toMerge);
                        deferredNodes.add(new DeferredResult(newNode, oldResult.originalDesire, oldResult.originalContext));
                    }
                } else {
                    // note that if the original node was in the defer queue,
                    // it does not get transfered to the final queue
                    logger.debug("Node already in merged graph for satisfaction: {}", toMerge.getLabel());
                }

                // update merge map so future nodes use this node as a dependency
                mergedMap.put(toMerge, newNode);
            }
        }
    }
    
    private Map<Set<Node>, Node> getDependencyOptions(CachedSatisfaction satisfaction) {
        // build a base map of dependency configurations to nodes for the provided
        // satisfaction, using the current state of the graph
        Map<Set<Node>, Node> options = new HashMap<Set<Node>, Node>();
        for (Node node: graph.getNodes()) {
            if (satisfaction.equals(node.getLabel())) {
                // accumulate all of its immediate dependencies
                Set<Node> option = new HashSet<Node>();
                for (Edge edge: graph.getOutgoingEdges(node)) {
                    option.add(edge.getTail());
                }
                options.put(option, node);
            }
        }
        return options;
    }

    /**
     * Resolve a desire and its dependencies, inserting them into the graph.
     * @param desire The desire to resolve.
     * @param parent The parent node (in the initial call, this is the root node).
     * @param graph The graph to store the results in.
     * @param context The context of {@code parent}.
     * @param defer The map of deferred nodes.
     * @throws SolverException if thtere is an error resolving the nodes.
     */
    private void resolveFully(Desire desire, Node parent, Graph graph, InjectionContext context, 
                              Map<Node, DeferredResult> defer) throws SolverException {
        // check context depth against max to detect likely dependency cycles
        if (context.getTypePath().size() > maxDepth) {
            throw new CyclicDependencyException(desire, "Maximum context depth of " + maxDepth + " was reached");
        }
        
        // resolve the current node
        //  - pair of pairs is a little awkward, but there's no triple type
        Resolution result = resolve(desire, context);
        Node newNode = new Node(new CachedSatisfaction(result.satisfaction, result.policy));
        
        // add the node to the graph, and connect it with its parent
        graph.addNode(newNode);
        graph.addEdge(new Edge(parent, newNode, result.desires));
        
        if (result.deferDependencies) {
            // push node onto deferred queue and skip its dependencies for now
            logger.debug("Deferring dependencies of {}", result.satisfaction);
            defer.put(newNode, new DeferredResult(newNode, desire, context));
        } else {
            for (Desire d: result.satisfaction.getDependencies()) {
                // complete the sub graph for the given desire
                // - the call to resolveFully() is responsible for adding the dependency edges
                //   so we don't need to process the returned node
                logger.debug("Attempting to satisfy dependency {} of {}", d, result.satisfaction);
                InjectionContext newContext = context.push(result.satisfaction, desire.getInjectionPoint().getAttributes());
                resolveFully(d, newNode, graph, newContext, defer);
            }
        }
    }
    
    private Resolution resolve(Desire desire, InjectionContext context) throws SolverException {
        Desire currentDesire = desire;
        CachePolicy policy = CachePolicy.NO_PREFERENCE;
        while(true) {
            logger.debug("Current desire: {}", currentDesire);
            
            BindingResult binding = null;
            for (BindingFunction bf: functions) {
                binding = bf.bind(context, currentDesire);
                if (binding != null && !context.getPriorDesires().contains(binding.getDesire())) {
                    // found a binding that hasn't been used before
                    break;
                }
            }
            
            boolean defer = false;
            boolean terminate = true;
            if (binding != null) {
                // update the prior desires
                context.recordDesire(currentDesire);
                currentDesire = binding.getDesire();
                
                terminate = binding.terminates();
                defer = binding.isDeferred();
                
                // upgrade policy if needed
                if (binding.getCachePolicy().compareTo(policy) > 0) {
                    policy = binding.getCachePolicy();
                }
            }
            
            if (terminate && currentDesire.isInstantiable()) {
                // push current desire so its included in resolved desires
                context.recordDesire(currentDesire);
                logger.info("Satisfied {} with {}", desire, currentDesire.getSatisfaction());
                
                // update cache policy if a specific policy hasn't yet been selected
                if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                    policy = currentDesire.getSatisfaction().getDefaultCachePolicy();
                    if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                        policy = defaultPolicy;
                    }
                }
                
                return new Resolution(currentDesire.getSatisfaction(), policy, context.getPriorDesires(), defer);
            } else if (binding == null) {
                // no more desires to process, it cannot be satisfied
                throw new UnresolvableDependencyException(currentDesire, context);
            }
        }
    }
    
    /*
     * Result tuple for resolve(Desire, InjectionContext)
     */
    private static class Resolution {
        private final Satisfaction satisfaction;
        private final CachePolicy policy;
        private final List<Desire> desires;
        private final boolean deferDependencies;
        
        public Resolution(Satisfaction satisfaction, CachePolicy policy, 
                          List<Desire> desires, boolean deferDependencies) {
            this.satisfaction = satisfaction;
            this.policy = policy;
            this.desires = desires;
            this.deferDependencies = deferDependencies;
        }
    }
    
    /*
     * Deferred results tuple
     */
    private static class DeferredResult {
        private final Desire originalDesire;
        private final Node deferredNode; // deps haven't been processed yet
        private final InjectionContext originalContext;
        
        public DeferredResult(Node deferredNode, 
                              Desire originalDesire, InjectionContext originalContext) {
            this.deferredNode = deferredNode;
            this.originalContext = originalContext;
            this.originalDesire = originalDesire;
        }
    }
}
