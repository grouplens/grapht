/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.solver;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.graph.DAGNodeBuilder;
import org.grouplens.grapht.graph.MergePool;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.NullSatisfaction;
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
    public static final Component ROOT_SATISFACTION =
            Component.create(new NullSatisfaction(Void.TYPE), CachePolicy.NO_PREFERENCE);

    /**
     * Get an initial injection context.
     * @return The context from the initial injection.
     */
    public static InjectionContext initialContext() {
        return InjectionContext.singleton(ROOT_SATISFACTION.getSatisfaction());
    }

    /**
     * Get a singleton root node for a dependency graph.
     * @return A root node for a dependency graph with no resolved objects.
     */
    public static DAGNode<Component,Dependency> rootNode() {
        return DAGNode.singleton(ROOT_SATISFACTION);
    }

    private final int maxDepth;
    private final CachePolicy defaultPolicy;

    private final List<BindingFunction> functions;
    private final List<BindingFunction> triggerFunctions;
    
    private DAGNode<Component,Dependency> graph;
    private SetMultimap<DAGNode<Component,Dependency>, DAGEdge<Component,Dependency>> backEdges;
    private MergePool<Component,Dependency> mergePool;

    /**
     * Create a DependencySolver that uses the given functions, and max
     * depth of the dependency graph.
     * 
     * @param bindFunctions The binding functions that control desire bindings
     * @param maxDepth A maximum depth of the graph before it's determined that
     *            a cycle exists
     * @throws IllegalArgumentException if maxDepth is less than 1
     * @throws NullPointerException if bindFunctions is null
     */
    DependencySolver(List<BindingFunction> bindFunctions,
                     List<BindingFunction> triggers,
                     CachePolicy defaultPolicy, int maxDepth) {
        Preconditions.notNull("bindFunctions", bindFunctions);
        Preconditions.notNull("defaultPolicy", defaultPolicy);
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("Max depth must be at least 1");
        }
        
        this.functions = new ArrayList<BindingFunction>(bindFunctions);
        this.triggerFunctions = new ArrayList<BindingFunction>(triggers);
        this.maxDepth = maxDepth;
        this.defaultPolicy = defaultPolicy;
        
        graph = DAGNode.singleton(ROOT_SATISFACTION);
        backEdges = HashMultimap.create();
        mergePool = MergePool.create();

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
    public DAGNode<Component, Dependency> getGraph() {
        return graph;
    }

    /**
     * Get the map of back-edges for circular dependencies.  Circular dependencies are only allowed
     * via provider injection, and only if {@link ProviderBindingFunction} is one of the binding
     * functions.  In such cases, there will be a back edge from the provider node to the actual
     * node being provided, and this map will report that edge.
     *
     * @return A snapshot of the map of back-edges.  This snapshot is entirely independent of the
     *         back edge map maintained by the dependency solver.
     */
    public SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> getBackEdges() {
        return ImmutableSetMultimap.copyOf(backEdges);
    }

    /**
     * Get the back edge for a particular node and desire, if one exists.
     * @return The back edge, or {@code null} if no edge exists.
     * @see #getBackEdges()
     */
    public synchronized DAGNode<Component, Dependency> getBackEdge(DAGNode<Component, Dependency> parent,
                                                                   Desire desire) {
        return backEdges.get(parent)
                .stream()
                .filter(e -> e.getLabel().hasInitialDesire(desire))
                .findFirst()
                .map(DAGEdge::getTail)
                .orElse(null);
    }

    /**
     * Get the root node.
     * @deprecated Use {@link #getGraph()} instead.
     */
    @Deprecated
    public DAGNode<Component, Dependency> getRootNode() {
        return graph;
    }
    
    /**
     * Update the dependency graph to include the given desire. An edge from the
     * root node to the desire's resolved satisfaction will exist after this is
     * finished.
     * 
     * @param desire The desire to include in the graph
     */
    public synchronized void resolve(Desire desire) throws ResolutionException {
        logger.info("Resolving desire: {}", desire);

        Queue<Deferral> deferralQueue = new ArrayDeque<Deferral>();

        // before any deferred nodes are processed, we use a synthetic root
        // and null original desire since nothing produced this root
        deferralQueue.add(new Deferral(rootNode(), initialContext()));

        while(!deferralQueue.isEmpty()) {
            Deferral current = deferralQueue.poll();
            DAGNode<Component, Dependency> parent = current.node;
            // deferred nodes are either root - depless - or having deferred dependencies
            assert parent.getOutgoingEdges().isEmpty();

            if (current.node.getLabel().equals(ROOT_SATISFACTION)) {
                Pair<DAGNode<Component, Dependency>, Dependency> rootNode =
                        resolveFully(desire, current.context, deferralQueue);
                // add this to the global graph
                graph = DAGNode.copyBuilder(graph)
                               .addEdge(mergePool.merge(rootNode.getLeft()),
                                        rootNode.getRight())
                               .build();
            } else if (graph.getReachableNodes().contains(parent)) {
                // the node needs to be re-scanned.  This means that it was not consolidated by
                // a previous merge operation.  This branch only arises with provider injection.
                Satisfaction sat = parent.getLabel().getSatisfaction();
                for (Desire d: sat.getDependencies()) {
                    logger.debug("Attempting to resolve deferred dependency {} of {}", d, sat);
                    // resolve the dependency
                    Pair<DAGNode<Component, Dependency>, Dependency> result =
                            resolveFully(d, current.context, deferralQueue);
                    // merge it in
                    DAGNode<Component, Dependency> merged = mergePool.merge(result.getLeft());
                    // now see if there's a real cycle
                    if (merged.getReachableNodes().contains(parent)) {
                        // parent node is referenced from merged, we have a circle!
                        // that means we need a back edge
                        backEdges.put(parent, DAGEdge.create(parent, merged, result.getRight()));
                    } else {
                        // an edge from parent to merged does not add a cycle
                        // we have to update graph right away so it's available to merge the next
                        // dependency
                        DAGNode<Component, Dependency> newP =
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

    private void replaceNode(DAGNode<Component,Dependency> old,
                             DAGNode<Component,Dependency> repl) {
        Map<DAGNode<Component,Dependency>,
                DAGNode<Component,Dependency>> memory = Maps.newHashMap();
        graph = graph.replaceNode(old, repl, memory);

        // loop over a snapshot of the list, replacing nodes
        Collection<DAGEdge<Component, Dependency>> oldBackEdges = backEdges.values();
        backEdges = HashMultimap.create();
        for (DAGEdge<Component,Dependency> edge: oldBackEdges) {
            DAGNode<Component,Dependency> newHead, newTail;
            newHead = memory.get(edge.getHead());
            if (newHead == null) {
                newHead = edge.getHead();
            }
            newTail = memory.get(edge.getTail());
            if (newTail == null) {
                newTail = edge.getTail();
            }
            DAGEdge<Component,Dependency> newEdge;
            if (newHead.equals(edge.getHead()) && newTail.equals(edge.getTail())) {
                newEdge = edge;
            } else {
                newEdge = DAGEdge.create(newHead, newTail, edge.getLabel());
            }
            backEdges.put(newHead, newEdge);
        }
    }

    /**
     * Rewrite a dependency graph using the rules in this solver.  The accumulated global graph and
     * back edges are ignored and not modified.
     * <p>Graph rewrite walks the graph, looking for nodes to rewrite.  If the desire that leads
     * to a node is matched by a trigger binding function, then it is resolved using the binding
     * functions and replaced with the resulting (merged) node.  Rewriting proceeds from the root
     * down, but does not consider the children of nodes generated by the rewriting process.</p>
     *
     * @param graph The graph to rewrite.
     * @return A rewritten version of the graph.
     */
    public DAGNode<Component,Dependency> rewrite(DAGNode<Component,Dependency> graph) throws ResolutionException {
        if (!graph.getLabel().getSatisfaction().getErasedType().equals(Void.TYPE)) {
            throw new IllegalArgumentException("only full dependency graphs can be rewritten");
        }

        logger.debug("rewriting graph with {} nodes", graph.getReachableNodes().size());
        // We proceed in three stages.
        Map<DAGEdge<Component, Dependency>, DAGEdge<Component,Dependency>> replacementSubtrees =
                Maps.newHashMap();
        walkGraphForReplacements(graph,
                                 InjectionContext.singleton(graph.getLabel().getSatisfaction()),
                                 replacementSubtrees);

        DAGNode<Component, Dependency> stage2 =
                graph.transformEdges(Functions.forMap(replacementSubtrees, null));

        logger.debug("merging rewritten graph");
        // Now we have a graph (stage2) with rewritten subtrees based on trigger rules
        // We merge this graph with the original to deduplicate.
        MergePool<Component,Dependency> pool = MergePool.create();
        pool.merge(graph);
        return pool.merge(stage2);
    }

    /**
     * Walk the graph, looking for replacements.
     * @param root The node to walk.
     * @param context The context leading to this node.
     * @param replacements The map of replacements to build. This maps edges to their replacement
     *                     targets and labels.
     * @throws ResolutionException If there is a resolution error rewriting the graph.
     */
    private void walkGraphForReplacements(DAGNode<Component, Dependency> root,
                                          InjectionContext context,
                                          Map<DAGEdge<Component, Dependency>, DAGEdge<Component, Dependency>> replacements) throws ResolutionException {
        assert context.getTailValue().getLeft().equals(root.getLabel().getSatisfaction());
        for (DAGEdge<Component, Dependency> edge: root.getOutgoingEdges()) {
            logger.debug("considering {} for replacement", edge.getTail().getLabel());
            Desire desire = edge.getLabel().getDesireChain().getInitialDesire();
            DesireChain chain = DesireChain.singleton(desire);
            Pair<DAGNode<Component, Dependency>, Dependency> repl = null;
            if (!edge.getLabel().isFixed()) {
                for (BindingFunction bf: triggerFunctions) {
                    BindingResult result = bf.bind(context, chain);
                    if (result != null) {
                        // resolve the node
                        // we could reuse the resolution, but perf savings isn't worth complexity
                        repl = resolveFully(desire, context, null);
                        break;
                    }
                }
            } else {
                logger.debug("{} is fixed, skipping", edge.getTail().getLabel());
            }
            if (repl == null) {
                // no trigger bindings, walk the node's children
                InjectionContext next = context.extend(edge.getTail()
                                                           .getLabel()
                                                           .getSatisfaction(),
                                                       edge.getLabel()
                                                           .getDesireChain()
                                                           .getInitialDesire()
                                                           .getInjectionPoint());
                walkGraphForReplacements(edge.getTail(), next, replacements);
            } else {
                // trigger binding, add a replacement
                logger.info("replacing {} with {}",
                            edge.getTail().getLabel(),
                            repl.getLeft().getLabel());
                replacements.put(edge, DAGEdge.create(root, repl.getLeft(), repl.getRight()));
            }
        }
    }

    /**
     * Resolve a desire and its dependencies, inserting them into the graph.
     *
     * @param desire The desire to resolve.
     * @param context The context of {@code parent}.
     * @param deferQueue The queue of node deferrals.
     * @throws ResolutionException if there is an error resolving the nodes.
     */
    private Pair<DAGNode<Component,Dependency>,Dependency>
    resolveFully(Desire desire, InjectionContext context, Queue<Deferral> deferQueue) throws ResolutionException {
        // check context depth against max to detect likely dependency cycles
        if (context.size() > maxDepth) {
            throw new CyclicDependencyException(desire, "Maximum context depth of " + maxDepth + " was reached");
        }
        
        // resolve the current node
        Resolution result = resolve(desire, context);

        InjectionContext newContext = context.extend(result.satisfaction, desire.getInjectionPoint());

        DAGNode<Component, Dependency> node;
        if (result.deferDependencies) {
            // extend node onto deferred queue and skip its dependencies for now
            logger.debug("Deferring dependencies of {}", result.satisfaction);
            node = DAGNode.singleton(result.makeSatisfaction());
            // FIXME Deferred and skippable bindings do not interact well
            deferQueue.add(new Deferral(node, newContext));
            return Pair.of(node, result.makeDependency());
        } else {
            return resolveDepsAndMakeNode(deferQueue, result, newContext);
        }
    }

    private Pair<DAGNode<Component, Dependency>,Dependency> resolveDepsAndMakeNode(Queue<Deferral> deferQueue,
                                                                                   Resolution result,
                                                                                   InjectionContext newContext) throws ResolutionException {
        DAGNode<Component, Dependency> node;// build up a node with its outgoing edges
        DAGNodeBuilder<Component,Dependency> nodeBuilder = DAGNode.newBuilder();
        nodeBuilder.setLabel(result.makeSatisfaction());
        for (Desire d: result.satisfaction.getDependencies()) {
            // complete the sub graph for the given desire
            // - the call to resolveFully() is responsible for adding the dependency edges
            //   so we don't need to process the returned node
            logger.debug("Attempting to satisfy dependency {} of {}", d, result.satisfaction);
            Pair<DAGNode<Component, Dependency>, Dependency> dep;
            try {
                dep = resolveFully(d, newContext, deferQueue);
            } catch (UnresolvableDependencyException ex) {
                if (!d.equals(ex.getDesireChain().getInitialDesire())) {
                    // this is for some other (deeper) desire, fail
                    throw ex;
                }
                // whoops, try to backtrack
                Resolution back = result.skippable ? result.backtrack() : null;
                if (back != null) {
                    InjectionContext popped = newContext.getLeading();
                    InjectionContext forked = InjectionContext.extend(popped, back.satisfaction,
                                                                      back.desires.getInitialDesire().getInjectionPoint());
                    return resolveDepsAndMakeNode(deferQueue, back, forked);
                } else if (result.backtracked || result.skippable) {
                    // the result is the result of backtracking, or could be, so make an error at this dependency
                    throw new UnresolvableDependencyException(result.desires, newContext.getLeading(), ex);
                } else {
                    throw ex;
                }
            }
            nodeBuilder.addEdge(dep);
        }
        node = nodeBuilder.build();
        return Pair.of(node, result.makeDependency());
    }

    private Resolution resolve(Desire desire, InjectionContext context) throws ResolutionException {
        DesireChain chain = DesireChain.singleton(desire);

        CachePolicy policy = CachePolicy.NO_PREFERENCE;
        boolean fixed = false;
        boolean skippable = false;

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
            boolean terminate = true; // so we stop if there is no binding
            if (binding != null) {
                // update the desire chain
                chain = chain.extend(binding.getDesire());

                terminate = binding.terminates(); // binding decides if we stop
                defer = binding.isDeferred();
                fixed |= binding.isFixed();
                skippable = binding.isSkippable();
                
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
                
                return new Resolution(chain.getCurrentDesire().getSatisfaction(), policy, chain, fixed, defer, skippable, false);
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
        private final boolean fixed;
        private final boolean deferDependencies;
        private final boolean skippable;
        private final boolean backtracked;

        public Resolution(Satisfaction satisfaction, CachePolicy policy, 
                          DesireChain desires, boolean fixed,
                          boolean deferDependencies,
                          boolean skippable,
                          boolean backtracked) {
            this.satisfaction = satisfaction;
            this.policy = policy;
            this.desires = desires;
            this.fixed = fixed;
            this.deferDependencies = deferDependencies;
            this.skippable = skippable;
            this.backtracked = backtracked;
        }

        public Component makeSatisfaction() {
            return Component.create(satisfaction, policy);
        }

        public Dependency makeDependency() {
            EnumSet<Dependency.Flag> flags = Dependency.Flag.emptySet();
            if (fixed) {
                flags.add(Dependency.Flag.FIXED);
            }
            return Dependency.create(desires, flags);
        }

        /**
         * Backtrack this resolution to skip a binding.
         *
         * @return The backtracked resolution, or {@code null} if the resolution cannot backtrack because doing so
         * would result in a non-instantiable resolution.
         * @throws IllegalArgumentException if attempting to backtrack would be invalid, because the resolution is
         *                                  not skippable.
         */
        public Resolution backtrack() {
            if (!skippable) {
                throw new IllegalArgumentException("unskippable resolution can't backtrack");
            }
            if (desires.size() <= 1) {
                throw new IllegalArgumentException("singleton desire chain can't backtrack");
            }
            DesireChain shrunk = desires.getPreviousDesireChain();
            if (shrunk.getCurrentDesire().isInstantiable()) {
                return new Resolution(shrunk.getCurrentDesire().getSatisfaction(),
                                      policy, // FIXME Backtrack the policy
                                      shrunk,
                                      fixed,  // FIXME If we allow skippability on non-default bindings, this is wrong
                                      deferDependencies, // FIXME same here
                                      false, true);
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return "(" + satisfaction + ", " + policy + ")";
        }
    }
    
    /*
     * Deferred results tuple
     */
    private static class Deferral {
        private final DAGNode<Component, Dependency> node;
        private final InjectionContext context;

        public Deferral(DAGNode<Component, Dependency> node,
                        InjectionContext context) {
            this.node = node;
            this.context = context;
        }
    }
}
