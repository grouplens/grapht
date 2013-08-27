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

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.AttributesImpl;
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

    /**
     * Get an initial injection context.
     * @return The context from the initial injection.
     */
    public static InjectionContext initialContext() {
        return InjectionContext.singleton(ROOT_SATISFACTION.getSatisfaction(),
                                          new AttributesImpl());
    }

    /**
     * Get a singleton root node for a dependency graph.
     * @return A root node for a dependency graph with no resolved objects.
     */
    public static DAGNode<CachedSatisfaction,DesireChain> rootNode() {
        return DAGNode.singleton(ROOT_SATISFACTION);
    }

    private final int maxDepth;
    private final CachePolicy defaultPolicy;

    private final List<BindingFunction> functions;
    
    private DAGNode<CachedSatisfaction,DesireChain> graph;
    private Map<Pair<DAGNode<CachedSatisfaction,DesireChain>,Desire>,
            DAGNode<CachedSatisfaction,DesireChain>> backEdges;

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
        
        graph = DAGNode.singleton(ROOT_SATISFACTION);
        backEdges = Maps.newHashMap();

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
     * Get the current full dependency graph. This consists of a synthetic root node with edges
     * to the resolutions of all dependencies passed to {@link #resolve(Desire)}.
     * @return The resolved dependency graph.
     */
    public DAGNode<CachedSatisfaction,DesireChain> getGraph() {
        return graph;
    }

    /**
     * Get the map of back-edges for circular dependencies.  Circular dependencies are only allowed
     * via provider injection, and only if {@link ProviderBindingFunction} is one of the binding
     * functions.  In such cases, there will be a back edge from the provider node to the actual
     * node being provided, and this map will report that edge.
     * @return A snapshot of the map of back-edges.
     */
    public ImmutableMap<Pair<DAGNode<CachedSatisfaction, DesireChain>, Desire>, DAGNode<CachedSatisfaction, DesireChain>> getBackEdges() {
        return ImmutableMap.copyOf(backEdges);
    }

    /**
     * Get the back edge for a particular node and desire, if one exists.
     * @return The back edge, or {@code null} if no edge exists.
     * @see #getBackEdges()
     */
    public synchronized DAGNode<CachedSatisfaction,DesireChain> getBackEdge(DAGNode<CachedSatisfaction, DesireChain> parent,
                                                                            Desire desire) {
        return backEdges.get(Pair.of(parent, desire));
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
    public synchronized void resolve(Desire desire) throws SolverException {
        logger.info("Resolving desire: {}", desire);

        Queue<Deferral> deferralQueue = new ArrayDeque<Deferral>();

        // before any deferred nodes are processed, we use a synthetic root
        // and null original desire since nothing produced this root
        deferralQueue.add(new Deferral(rootNode(), initialContext()));

        while(!deferralQueue.isEmpty()) {
            Deferral current = deferralQueue.poll();
            DAGNode<CachedSatisfaction, DesireChain> parent = current.node;
            // deferred nodes are either root - depless - or having deferred dependencies
            assert parent.getOutgoingEdges().isEmpty();

            if (current.node.getLabel().equals(ROOT_SATISFACTION)) {
                DAGNodeBuilder<CachedSatisfaction,DesireChain> bld = DAGNode.copyBuilder(parent);
                bld.addEdge(resolveFully(desire, current.context, deferralQueue));
                graph = merge(bld.build(), true);
            } else if (graph.getReachableNodes().contains(parent)) {
                // the node needs to be re-scanned. Because parent nodes have no out edges, they
                // are unmodified by merge and will be preserved.
                Satisfaction sat = current.node.getLabel().getSatisfaction();
                for (Desire d: sat.getDependencies()) {
                    logger.debug("Attempting to resolve deferred dependency {} of {}", d, sat);
                    // resolve the dependency
                    Pair<DAGNode<CachedSatisfaction, DesireChain>, DesireChain> result =
                            resolveFully(d, current.context, deferralQueue);
                    // merge it in
                    DAGNode<CachedSatisfaction, DesireChain> merged = merge(result.getLeft(), false);
                    // now see if there's a real cycle
                    if (merged.getReachableNodes().contains(parent)) {
                        // parent node is referenced from merged, we have a circle!
                        // that means we need a back edge
                        // this assertion should be true, don't know why it isn't
                        // assert graph.getReachableNodes().contains(merged);
                        backEdges.put(Pair.of(parent, d), merged);
                    } else {
                        // an edge from parent to merged does not add a cycle
                        // we have to update graph right away so it's available to merge the next
                        // dependency
                        DAGNode<CachedSatisfaction, DesireChain> newP =
                                DAGNode.copyBuilder(parent)
                                       .addEdge(merged, result.getRight())
                                       .build();
                        replaceNode(parent, newP);
                        parent = newP;
                    }
                }
            } else {
                // node unreachable - it's a leftover or unneeded deferral
                logger.debug("node {} not in graph, ignoring", parent);
            }
        }
    }

    private void replaceNode(DAGNode<CachedSatisfaction,DesireChain> old, DAGNode<CachedSatisfaction,DesireChain> repl) {
        Map<DAGNode<CachedSatisfaction,DesireChain>,
                DAGNode<CachedSatisfaction,DesireChain>> memory = Maps.newHashMap();
        graph = graph.replaceNode(old, repl, memory);
        // loop over a snapshot of the list, replacing nodes
        for (Map.Entry<Pair<DAGNode<CachedSatisfaction,DesireChain>,Desire>,DAGNode<CachedSatisfaction,DesireChain>> e: Lists.newArrayList(backEdges.entrySet())) {
            Pair<DAGNode<CachedSatisfaction, DesireChain>, Desire> key = e.getKey();
            DAGNode<CachedSatisfaction, DesireChain> value = e.getValue();
            DAGNode<CachedSatisfaction, DesireChain> nk, nv;
            nk = memory.get(key.getLeft());
            nv = memory.get(value);
            if (nk != null || nv != null) {
                // need to transform this entry
                backEdges.put(nk == null ? key : Pair.of(nk, key.getRight()),
                              nv == null ? value : nv);
            }
        }
        // trim out unused nodes
        backEdges.keySet().retainAll(graph.getReachableNodes());
    }

    /**
     * Merge a graph, resulting from a resolve, into the global graph.
     *
     * @param tree The unmerged graph.
     * @param mergeRoot Whether to merge this with the root of the global graph.  If {@code true},
     *                  the outgoing edges of {@code tree} are added to the outgoing edges of
     *                  the root of the global graph and the resulting graph returned.
     */
    private DAGNode<CachedSatisfaction,DesireChain> merge(DAGNode<CachedSatisfaction,DesireChain> tree,
                                                          boolean mergeRoot) {
        List<DAGNode<CachedSatisfaction, DesireChain>> sorted = tree.getSortedNodes();

        Map<Pair<CachedSatisfaction,Set<DAGNode<CachedSatisfaction,DesireChain>>>,
                DAGNode<CachedSatisfaction,DesireChain>> nodeTable = Maps.newHashMap();
        for (DAGNode<CachedSatisfaction,DesireChain> node: graph.getReachableNodes()) {
            Pair<CachedSatisfaction,Set<DAGNode<CachedSatisfaction,DesireChain>>> key =
                    Pair.of(node.getLabel(), node.getAdjacentNodes());
            assert !nodeTable.containsKey(key);
            nodeTable.put(key, node);
        }
        
        // Look up each node's dependencies in the merged graph, since we sorted
        // by reverse depth we can guarantee that dependencies have already
        // been merged
        Map<DAGNode<CachedSatisfaction,DesireChain>,
                DAGNode<CachedSatisfaction,DesireChain>> mergedMap = Maps.newHashMap();
        for (DAGNode<CachedSatisfaction, DesireChain> toMerge: sorted) {
            CachedSatisfaction sat = toMerge.getLabel();
            // Accumulate the set of dependencies for this node, filtering
            // them through the previous level map
            Set<DAGNode<CachedSatisfaction, DesireChain>> dependencies =
                    FluentIterable.from(toMerge.getOutgoingEdges())
                                  .transform(DAGEdge.<CachedSatisfaction,DesireChain>extractTail())
                                  .transform(Functions.forMap(mergedMap))
                                  .toSet();

            DAGNode<CachedSatisfaction, DesireChain> newNode =
                    nodeTable.get(Pair.of(sat, dependencies));
            if (newNode == null) {
                DAGNodeBuilder<CachedSatisfaction,DesireChain> bld = DAGNode.newBuilder();
                // this configuration for the satisfaction has not been seen before
                // - add it to merged graph, and connect to its dependencies

                boolean changed = false;
                bld.setLabel(sat);
                logger.debug("Adding new node to merged graph for satisfaction: {}", sat);

                for (DAGEdge<CachedSatisfaction, DesireChain> dep: toMerge.getOutgoingEdges()) {
                    // add the edge with the new head and the previously merged tail
                    // List<Desire> is downsized to the first Desire, too
                    DAGNode<CachedSatisfaction, DesireChain> filtered = mergedMap.get(dep.getTail());
                    bld.addEdge(filtered, dep.getLabel());
                    changed |= !filtered.equals(dep.getTail());
                }

                if (changed) {
                    newNode = bld.build();
                } else {
                    // no edges were changed, leave the node unmodified
                    newNode = toMerge;
                }
                nodeTable.put(Pair.of(sat, dependencies), newNode);
            } else {
                logger.debug("Node already in merged graph for satisfaction: {}", toMerge.getLabel());
            }

            // update merge map so future nodes use this node as a dependency
            mergedMap.put(toMerge, newNode);
        }

        // now time to build up the last node
        if (mergeRoot) {
            DAGNodeBuilder<CachedSatisfaction,DesireChain> bld = DAGNode.copyBuilder(graph);
            for (DAGEdge<CachedSatisfaction,DesireChain> edge: mergedMap.get(tree).getOutgoingEdges()) {
                bld.addEdge(edge.getTail(), edge.getLabel());
            }
            return bld.build();
        } else {
            return mergedMap.get(tree);
        }
    }

    /**
     * Resolve a desire and its dependencies, inserting them into the graph.
     *
     * @param desire The desire to resolve.
     * @param context The context of {@code parent}.
     * @param deferQueue The queue of node deferrals.
     * @throws SolverException if there is an error resolving the nodes.
     */
    private Pair<DAGNode<CachedSatisfaction,DesireChain>,DesireChain>
    resolveFully(Desire desire, InjectionContext context, Queue<Deferral> deferQueue) throws SolverException {
        // check context depth against max to detect likely dependency cycles
        if (context.size() > maxDepth) {
            throw new CyclicDependencyException(desire, "Maximum context depth of " + maxDepth + " was reached");
        }
        
        // resolve the current node
        //  - pair of pairs is a little awkward, but there's no triple type
        Resolution result = resolve(desire, context);
        CachedSatisfaction sat = new CachedSatisfaction(result.satisfaction, result.policy);

        InjectionContext newContext = context.extend(result.satisfaction, desire.getInjectionPoint().getAttributes());

        DAGNode<CachedSatisfaction,DesireChain> node;
        if (result.deferDependencies) {
            // extend node onto deferred queue and skip its dependencies for now
            logger.debug("Deferring dependencies of {}", result.satisfaction);
            node = DAGNode.singleton(sat);
            deferQueue.add(new Deferral(node, newContext));
        } else {
            // build up a node with its outgoing edges
            DAGNodeBuilder<CachedSatisfaction,DesireChain> nodeBuilder = DAGNode.newBuilder();
            nodeBuilder.setLabel(sat);
            for (Desire d: result.satisfaction.getDependencies()) {
                // complete the sub graph for the given desire
                // - the call to resolveFully() is responsible for adding the dependency edges
                //   so we don't need to process the returned node
                logger.debug("Attempting to satisfy dependency {} of {}", d, result.satisfaction);
                nodeBuilder.addEdge(resolveFully(d, newContext, deferQueue));
            }
            node = nodeBuilder.build();
        }

        return Pair.of(node, result.desires);
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
    private static class Deferral {
        private final DAGNode<CachedSatisfaction, DesireChain> node;
        private final InjectionContext context;

        public Deferral(DAGNode<CachedSatisfaction, DesireChain> node,
                        InjectionContext context) {
            this.node = node;
            this.context = context;
        }
    }
}
