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
package org.grouplens.grapht;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jetbrains.annotations.Nullable;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Container for dependency-injected components.  A container is the scope of memoization, so
 * components with a cache policy of {@link CachePolicy#MEMOIZE} will share an instance so long
 * as they are instantiated by the same instantiator.
 *
 * @since 0.9
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class InjectionContainer {
    private static final Logger logger = LoggerFactory.getLogger(InjectionContainer.class);

    private final CachePolicy defaultCachePolicy;
    private final Map<DAGNode<Component, Dependency>, Instantiator> providerCache;
    private final LifecycleManager manager;

    /**
     * Create a new instantiator with a default policy of {@code MEMOIZE}.
     * @return The instantiator.
     */
    public static InjectionContainer create() {
        return create(CachePolicy.MEMOIZE);
    }

    /**
     * Create a new instantiator without a lifecycle manager.
     * @param dft The default cache policy.
     * @return The instantiator.
     */
    public static InjectionContainer create(CachePolicy dft) {
        return new InjectionContainer(dft, null);
    }

    /**
     * Create a new instantiator.
     * @param dft The default cache policy.
     * @param mgr The lifecycle manager.
     * @return The instantiator.
     */
    public static InjectionContainer create(CachePolicy dft, LifecycleManager mgr) {
        return new InjectionContainer(dft, mgr);
    }

    private InjectionContainer(CachePolicy dft, LifecycleManager mgr) {
        defaultCachePolicy = dft;
        providerCache = new WeakHashMap<DAGNode<Component, Dependency>, Instantiator>();
        manager = mgr;
    }

    /**
     * Get a provider that, when invoked, will return an instance of the component represented
     * by a graph.
     *
     *
     * @param node The graph.
     * @return A provider to instantiate {@code graph}.
     * @see #makeInstantiator(DAGNode, SetMultimap)
     */
    public Instantiator makeInstantiator(DAGNode<Component, Dependency> node) {
        return makeInstantiator(node, ImmutableSetMultimap.<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>>of());
    }

    /**
     * Get a provider that, when invoked, will return an instance of the component represented
     * by a graph with back edges.  The provider will implement the cache policy, so cached nodes
     * will return a memoized provider.
     *
     * @param node The graph.
     * @param backEdges A multimap of back edges for cyclic dependencies.
     * @return A provider to instantiate {@code graph}.
     */
    public Instantiator makeInstantiator(DAGNode<Component, Dependency> node,
                                         SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges) {
        Instantiator cached;
        synchronized (providerCache) {
            cached = providerCache.get(node);
        }
        if (cached == null) {
            logger.debug("Node has not been memoized, instantiating: {}", node.getLabel());

            Map<Desire, Instantiator> depMap = makeDependencyMap(node, backEdges);

            Instantiator raw = node.getLabel().getSatisfaction().makeInstantiator(depMap, manager);

            CachePolicy policy = node.getLabel().getCachePolicy();
            if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                policy = defaultCachePolicy;
            }
            if (policy.equals(CachePolicy.MEMOIZE)) {
                // enforce memoization on providers for MEMOIZE policy
                cached = Instantiators.memoize(raw);
            } else {
                // Satisfaction.makeInstantiator() returns providers that are expected
                // to create new instances with each invocation
                assert policy.equals(CachePolicy.NEW_INSTANCE);
                cached = raw;
            }
            synchronized (providerCache) {
                if (!providerCache.containsKey(node)) {
                    providerCache.put(node, cached);
                } else {
                    logger.debug("two threads built instantiator for {}, discarding 2nd build", node);
                    cached = providerCache.get(node);
                }
            }
        }
        return cached;
    }

    private Map<Desire, Instantiator> makeDependencyMap(DAGNode<Component, Dependency> node, SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges) {
        Set<DAGEdge<Component,Dependency>> edges = node.getOutgoingEdges();
        if (backEdges.containsKey(node)) {
            ImmutableSet.Builder<DAGEdge<Component,Dependency>> bld = ImmutableSet.builder();
            edges = bld.addAll(edges)
                       .addAll(backEdges.get(node))
                       .build();
        }

        ImmutableSet.Builder<Desire> desires = ImmutableSet.builder();
        for (DAGEdge<Component,Dependency> edge: edges) {
            desires.add(edge.getLabel().getInitialDesire());
        }
        return Maps.asMap(desires.build(), new DepLookup(edges, backEdges));
    }

    /**
     * Get the lifecycle manager for this container.
     * @return The lifecycle manager for the container.
     */
    @Nullable
    public LifecycleManager getLifecycleManager() {
        return manager;
    }

    /**
     * Function to look up a desire in a set of dependency edges.
     */
    private class DepLookup implements Function<Desire,Instantiator> {
        private final Set<DAGEdge<Component, Dependency>> edges;
        private final SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges;

        /**
         * Construct a depenency lookup funciton.
         * @param edges The set of edges to consult.
         * @param backEdges The back edge map (to pass to {@link #makeInstantiator(DAGNode,SetMultimap)}).
         */
        public DepLookup(Set<DAGEdge<Component,Dependency>> edges,
                         SetMultimap<DAGNode<Component, Dependency>, DAGEdge<Component, Dependency>> backEdges) {
            this.edges = edges;
            this.backEdges = backEdges;
        }

        @Nullable
        @Override
        public Instantiator apply(@Nullable Desire input) {
            for (DAGEdge<Component,Dependency> edge: edges) {
                if (edge.getLabel().getInitialDesire().equals(input)) {
                    return makeInstantiator(edge.getTail(), backEdges);
                }
            }
            return null;
        }
    }
}
