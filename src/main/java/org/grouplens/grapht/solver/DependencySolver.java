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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.graph.*;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.NullSatisfaction;
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
    public static final CachedSatisfaction ROOT_SATISFACTION =
            new CachedSatisfaction(new NullSatisfaction(Void.TYPE), CachePolicy.NO_PREFERENCE);
    
    private final int maxDepth;
    private final CachePolicy defaultPolicy;

    private final List<BindingFunction> functions;
    
    private DAGNode<CachedSatisfaction,DesireChain> graph;

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

        graph = DAGNode.singleton(ROOT_SATISFACTION);

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
    public DAGNode<CachedSatisfaction,DesireChain> getGraph() {
        return graph;
    }

    /**
     * Get the root node.
     * @deprecated Use {@link #getGraph()} instead.
     */
    @Deprecated
    public DAGNode<CachedSatisfaction,DesireChain> getRootNode() {
        return graph;
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
            InjectionContext initialContext = InjectionContext.initial();
            deferredNodes.add(new DeferredResult(ROOT_SATISFACTION, null, initialContext));
            
            while(!deferredNodes.isEmpty()) {
                DeferredResult treeRoot = deferredNodes.poll();
                
                Map<CachedSatisfaction, DeferredResult> toResolve =
                        Maps.newIdentityHashMap();

                DAGNodeBuilder<CachedSatisfaction,DesireChain> bld =
                        DAGNode.newBuilder(treeRoot.satisfaction);

                if (treeRoot.satisfaction == ROOT_SATISFACTION) {
                    bld.addEdge(resolveFully(desire, treeRoot.originalContext, toResolve));
                } else {
                    Satisfaction deferredSatisfaction = treeRoot.satisfaction.getSatisfaction();
                    for (Desire d: deferredSatisfaction.getDependencies()) {
                        logger.debug("Attempting to resolve deferred dependency {} of {}", d, deferredSatisfaction);
                        InjectionContext newContext = treeRoot.originalContext.extend(deferredSatisfaction, treeRoot.originalDesire.getInjectionPoint().getAttributes());
                        bld.addEdge(resolveFully(d, newContext, toResolve));
                    }
                }
                merge(bld.build(), toResolve);
            }
        } catch(SolverException e) {
            logger.error("Error while resolving: {}", e.getMessage());
            throw e;
        } catch(RuntimeException e) {
            logger.error("Error while resolving: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Merge a graph, resulting from a resolve, into the global graph.
     *
     * @param tree The unmerged graph.
     * @param defer The map of deferred nodes.  If a node to be merged is in this, it is queued
     */
    private void merge(DAGNode<CachedSatisfaction,DesireChain> tree,
                       Map<CachedSatisfaction, DeferredResult> defer) {
        List<DAGNode<CachedSatisfaction, DesireChain>> sorted = tree.getSortedNodes();
        
        // Look up each node's dependencies in the merged graph, since we sorted
        // by reverse depth we can guarantee that dependencies have already
        // been merged
        Map<DAGNode<CachedSatisfaction,DesireChain>,
                DAGNode<CachedSatisfaction,DesireChain>> mergedMap = Maps.newHashMap();
        for (DAGNode<CachedSatisfaction, DesireChain> toMerge: sorted) {
            if (toMerge == tree && tree.getLabel() == ROOT_SATISFACTION) {
                // This is the synthetic root of the tree.
                // We replace the root node of the tree with the root in the merged graph.
                for (DAGEdge<CachedSatisfaction, DesireChain> oldEdge: tree.getOutgoingEdges()) {
                    DAGNode<CachedSatisfaction, DesireChain> newTail = mergedMap.get(oldEdge.getTail());
                    assert newTail != null; // like below, it must have been merged previously

                    // there can be at most one edge with this label in the merged
                    // graph because this is at the root context, and there is no
                    // way to cause their configurations to diverge
                    if (graph.getOutgoingEdge(oldEdge.getLabel()) == null) {
                        // this desire is not in the merged graph
                        graph = DAGNode.copyBuilder(graph)
                                       .addEdge(newTail, oldEdge.getLabel())
                                       .build();
                    }
                }
            } else {
                // Get all previously seen dependency configurations for this satisfaction
                Map<Set<DAGNode<CachedSatisfaction, DesireChain>>, DAGNode<CachedSatisfaction, DesireChain>> dependencyOptions = getDependencyOptions(toMerge.getLabel());
                
                // Accumulate the set of dependencies for this node, filtering
                // them through the previous level map
                Set<DAGNode<CachedSatisfaction, DesireChain>> dependencies = Sets.newHashSet();
                for (DAGEdge<CachedSatisfaction,DesireChain> dep: toMerge.getOutgoingEdges()) {
                    // levelMap converts from the tree to the merged graph
                    DAGNode<CachedSatisfaction, DesireChain> filtered = mergedMap.get(dep.getTail());
                    assert filtered != null; // all dependencies should have been merged previously
                    dependencies.add(filtered);
                }
                
                DAGNode<CachedSatisfaction, DesireChain> newNode = dependencyOptions.get(dependencies);
                if (newNode == null) {
                    DAGNodeBuilder<CachedSatisfaction,DesireChain> bld = DAGNode.newBuilder();
                    // this configuration for the satisfaction has not been seen before
                    // - add it to merged graph, and connect to its dependencies

                    if (toMerge == tree) {
                        // non-cyclic deferred node from a prior resolution, so toMerge
                        // is already in this graph (but had no edges, so it didn't
                        // get identified correctly)
                        // FIXME This is probably broken
                        throw new RuntimeException("hideously broken code path");
                        // bld.setLabel(toMerge.getLabel());
                        // logger.debug("Linking deferred satisfaction to graph: {}", toMerge.getLabel());
                    } else {
                        // create a new node
                        bld.setLabel(toMerge.getLabel());
                        logger.debug("Adding new node to merged graph for satisfaction: {}", toMerge.getLabel());
                    }

                    for (DAGEdge<CachedSatisfaction, DesireChain> dep: toMerge.getOutgoingEdges()) {
                        // add the edge with the new head and the previously merged tail
                        // List<Desire> is downsized to the first Desire, too
                        DAGNode<CachedSatisfaction, DesireChain> filtered = mergedMap.get(dep.getTail());
                        bld.addEdge(filtered, dep.getLabel());
                    }

                    // if the original node was in the defer queue, insert
                    // merged node into the final queue
                    if (defer.containsKey(toMerge)) {
                        DeferredResult oldResult = defer.get(toMerge);
                        // deferredNodes.add(new DeferredResult(newNode, oldResult.originalDesire, oldResult.originalContext));
                    } else {
                        newNode = bld.build();
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
    
    private Map<Set<DAGNode<CachedSatisfaction, DesireChain>>, DAGNode<CachedSatisfaction, DesireChain>> getDependencyOptions(CachedSatisfaction satisfaction) {
        // build a base map of dependency configurations to nodes for the provided
        // satisfaction, using the current state of the graph
        Map<Set<DAGNode<CachedSatisfaction, DesireChain>>, DAGNode<CachedSatisfaction, DesireChain>> options =
                Maps.newHashMap();
        for (DAGNode<CachedSatisfaction, DesireChain> node: graph.getReachableNodes()) {
            if (satisfaction.equals(node.getLabel())) {
                // accumulate all of its immediate dependencies
                Set<DAGNode<CachedSatisfaction, DesireChain>> option = Sets.newHashSet();
                for (DAGEdge<CachedSatisfaction, DesireChain> edge: node.getOutgoingEdges()) {
                    option.add(edge.getTail());
                }
                options.put(option, node);
            }
        }
        return options;
    }

    /**
     * Resolve a desire and its dependencies, inserting them into the graph.
     *
     * @param desire The desire to resolve.
     * @param context The context of {@code parent}.
     * @param defer The map of deferred nodes.
     * @throws SolverException if there is an error resolving the nodes.
     */
    private Pair<DAGNode<CachedSatisfaction,DesireChain>,DesireChain>
    resolveFully(Desire desire, InjectionContext context, Map<CachedSatisfaction, DeferredResult> defer) throws SolverException {
        // check context depth against max to detect likely dependency cycles
        if (context.size() > maxDepth) {
            throw new CyclicDependencyException(desire, "Maximum context depth of " + maxDepth + " was reached");
        }
        
        // resolve the current node
        //  - pair of pairs is a little awkward, but there's no triple type
        Resolution result = resolve(desire, context);
        CachedSatisfaction sat = new CachedSatisfaction(result.satisfaction, result.policy);
        DAGNodeBuilder<CachedSatisfaction,DesireChain> nodeBuilder = DAGNode.newBuilder();
        nodeBuilder.setLabel(sat);

        if (result.deferDependencies) {
            // extend node onto deferred queue and skip its dependencies for now
            logger.debug("Deferring dependencies of {}", result.satisfaction);
            defer.put(sat, new DeferredResult(sat, desire, context));
        } else {
            for (Desire d: result.satisfaction.getDependencies()) {
                // complete the sub graph for the given desire
                // - the call to resolveFully() is responsible for adding the dependency edges
                //   so we don't need to process the returned node
                logger.debug("Attempting to satisfy dependency {} of {}", d, result.satisfaction);
                InjectionContext newContext = context.extend(result.satisfaction, desire.getInjectionPoint().getAttributes());
                Pair<DAGNode<CachedSatisfaction, DesireChain>, DesireChain> depResult;
                depResult = resolveFully(d, newContext, defer);
                nodeBuilder.addEdge(depResult);
            }
        }

        return Pair.of(nodeBuilder.build(), result.desires);
    }
    
    private Resolution resolve(Desire desire, InjectionContext context) throws SolverException {
        DesireChain chain = DesireChain.singleton(desire);

        CachePolicy policy = CachePolicy.NO_PREFERENCE;
        while(true) {
            logger.debug("Current desire: {}", chain.getCurrentDesire());
            
            BindingResult binding = null;
            for (BindingFunction bf: functions) {
                binding = bf.bind(context, chain);
                if (binding != null && !chain.getPreviousDesires().contains(binding.getDesire())) {
                    // found a binding that hasn't been used before
                    break;
                }
            }
            
            boolean defer = false;
            boolean terminate = true;
            if (binding != null) {
                // update the desire chain
                chain = chain.extend(binding.getDesire());

                terminate = binding.terminates();
                defer = binding.isDeferred();
                
                // upgrade policy if needed
                if (binding.getCachePolicy().compareTo(policy) > 0) {
                    policy = binding.getCachePolicy();
                }
            }
            
            if (terminate && chain.getCurrentDesire().isInstantiable()) {
                logger.info("Satisfied {} with {}", desire, chain.getCurrentDesire().getSatisfaction());
                
                // update cache policy if a specific policy hasn't yet been selected
                if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                    policy = chain.getCurrentDesire().getSatisfaction().getDefaultCachePolicy();
                    if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                        policy = defaultPolicy;
                    }
                }
                
                return new Resolution(chain.getCurrentDesire().getSatisfaction(), policy, chain, defer);
            } else if (binding == null) {
                // no more desires to process, it cannot be satisfied
                throw new UnresolvableDependencyException(chain, context);
            }
        }
    }
    
    /*
     * Result tuple for resolve(Desire, InjectionContext)
     */
    private static class Resolution {
        private final Satisfaction satisfaction;
        private final CachePolicy policy;
        private final DesireChain desires;
        private final boolean deferDependencies;
        
        public Resolution(Satisfaction satisfaction, CachePolicy policy, 
                          DesireChain desires, boolean deferDependencies) {
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
        private final CachedSatisfaction satisfaction; // deps haven't been processed yet
        private final InjectionContext originalContext;
        
        public DeferredResult(CachedSatisfaction satisfaction,
                              Desire originalDesire, InjectionContext originalContext) {
            this.satisfaction = satisfaction;
            this.originalContext = originalContext;
            this.originalDesire = originalDesire;
        }
    }
}
