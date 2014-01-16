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

import com.google.common.base.Predicate;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.util.MemoizingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * DefaultInjector is the default Injector implementation. When resolving the
 * dependency graph for a desire, a "context" is built which consists of an
 * ordering of qualified types that satisfy each dependency. The DefaultInjector
 * uses the {@link DependencySolver} to manage dependency resolution. New
 * injectors can easily be built to also use this solver.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public class DefaultInjector implements Injector {
    private static final Logger logger = LoggerFactory.getLogger(DefaultInjector.class);
    
    private final DependencySolver solver;
    private final Map<DAGNode<CachedSatisfaction, DesireChain>, Provider<?>> providerCache;
    

    /**
     * <p>
     * Create a new DefaultInjector. The created resolver will use a max
     * dependency depth of 100 to estimate if there are cycles in the dependency
     * hierarchy. Bindings with a NO_PREFERENCE cache policy will be treated as
     * NEW_INSTANCE.
     * 
     * @param functions The BindingFunctions to use, ordered with highest
     *            priority function first
     * @throws NullPointerException if spi or functions ar enull
     */
    public DefaultInjector(BindingFunction... functions) {
        this(CachePolicy.MEMOIZE, functions);
    }
    
    /**
     * <p>
     * Create a new DefaultInjector. The created resolver will use a max
     * dependency depth of 100 to estimate if there are cycles in the dependency
     * hierarchy. Bindings with a NO_PREFERENCE cache policy will use
     * <tt>defaultPolicy</tt>.
     * 
     * @param defaultPolicy The CachePolicy used in place of NO_PREFERENCE
     * @param functions The BindingFunctions to use, ordered with highest
     *            priority functions first
     * @throws IllegalArgumentException if defaultPolicy is NO_PREFERENCE
     * @throws NullPointerException if spi or functions are null
     */
    public DefaultInjector(CachePolicy defaultPolicy, BindingFunction... functions) {
        this(defaultPolicy, 100, functions);
    }

    /**
     * <p>
     * Create a new DefaultInjector. <tt>maxDepth</tt> represents the maximum
     * depth of the dependency hierarchy before it is assume that there is a
     * cycle. Bindings with a NO_PREFERENCE cache policy will use
     * <tt>defaultPolicy</tt>.
     * <p>
     * This constructor can be used to increase this depth in the event that
     * configuration requires it, although for most purposes the default 100
     * should be sufficient.
     * 
     * @param defaultPolicy The CachePolicy used in place of NO_PREFERENCE
     * @param maxDepth The maximum depth of the dependency hierarchy
     * @param functions The BindingFunctions to use, ordered with highest
     *            priority functions first
     * @throws IllegalArgumentException if maxDepth is less than 1, or if
     *             defaultPolicy is NO_PREFERENCE
     * @throws NullPointerException if spi or functions are null
     */
    public DefaultInjector(CachePolicy defaultPolicy, int maxDepth, BindingFunction... functions) {
        if (defaultPolicy.equals(CachePolicy.NO_PREFERENCE)) {
            throw new IllegalArgumentException("Default CachePolicy cannot be NO_PREFERENCE");
        }

        solver = DependencySolver.newBuilder()
                                 .addBindingFunctions(functions)
                                 .setDefaultPolicy(defaultPolicy)
                                 .setMaxDepth(maxDepth)
                                 .build();
        providerCache = new HashMap<DAGNode<CachedSatisfaction, DesireChain>, Provider<?>>();
    }
    
    /**
     * @return The DependencySolver backing this injector
     */
    public DependencySolver getSolver() {
        return solver;
    }
    
    @Override
    public <T> T getInstance(Class<T> type) {
        return getInstance(null, type);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Annotation qualifier, Class<T> type) {
        // All Provider cache access, graph resolution, etc. occur
        // within this exclusive lock so we know everything is thread safe
        // albeit in a non-optimal way.
        synchronized(this) {
            Desire desire = Desires.create(qualifier, type, false);

            Predicate<DesireChain> pred = DesireChain.hasInitialDesire(desire);

            // check if the desire is already in the graph
            DAGEdge<CachedSatisfaction, DesireChain> resolved =
                    solver.getGraph().getOutgoingEdgeWithLabel(pred);

            // The edge is only non-null if getInstance() has been called before,
            // it may be present in the graph at a deeper node. If that's the case
            // it will be properly merged after regenerating the graph at the root context.
            if (resolved == null) {
                logger.info("Must resolve desire: {}", desire);
                try {
                    solver.resolve(desire);
                } catch(SolverException e) {
                    throw new InjectionException(type, null, e);
                }
                resolved = solver.getGraph().getOutgoingEdgeWithLabel(pred);
            }

            // Check if the provider for the resolved node is in our cache
            DAGNode<CachedSatisfaction, DesireChain> resolvedNode = resolved.getTail();
            return (T) getProvider(resolvedNode).get();
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Provider<?> getProvider(DAGNode<CachedSatisfaction, DesireChain> node) {
        Provider<?> cached = providerCache.get(node);
        if (cached == null) {
            logger.debug("Node has not been memoized, instantiating: {}", node.getLabel());
            Provider<?> raw = node.getLabel().getSatisfaction().makeProvider(new DesireProviderMapper(node));
            
            CachePolicy policy = node.getLabel().getCachePolicy();
            if (policy.equals(CachePolicy.MEMOIZE)) {
                // enforce memoization on providers for MEMOIZE policy
                cached = new MemoizingProvider(raw);
            } else {
                // Satisfaction.makeProvider() returns providers that are expected
                // to create new instances with each invocation
                assert policy.equals(CachePolicy.NEW_INSTANCE);
                cached = raw;
            }
            providerCache.put(node, cached);
        }
        return cached;
    }
    
    private class DesireProviderMapper implements ProviderSource {
        private final DAGNode<CachedSatisfaction, DesireChain> forNode;
        
        public DesireProviderMapper(DAGNode<CachedSatisfaction, DesireChain> forNode) {
            this.forNode = forNode;
        }
        
        @Override
        public Provider<?> apply(Desire desire) {
            DAGEdge<CachedSatisfaction, DesireChain> edge =
                    forNode.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(desire));
            DAGNode<CachedSatisfaction, DesireChain> dependency;
            if (edge != null) {
                dependency = edge.getTail();
            } else {
                dependency = solver.getBackEdge(forNode, desire);
            }
            if (dependency == null) {
                // we have an unresolved graph, that can't happen
                throw new RuntimeException("unresolved dependency " + desire + " for " + forNode.getLabel().getSatisfaction());
            }
            return getProvider(dependency);
        }
    }
}
