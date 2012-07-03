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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * DefaultInjector is the default Injector implementation. When resolving the
 * dependency graph for a desire, a "context" is built which consists of an
 * ordering of qualified types that satisfy each dependency. The DefaultInjector
 * uses the {@link DependencySolver} to manage dependency resolution. New
 * injectors can easily be built to also use this solver.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class DefaultInjector implements Injector {
    private static final Logger logger = LoggerFactory.getLogger(DefaultInjector.class);
    
    private final InjectSPI spi;
    private final DependencySolver solver;
    private final Map<Node<Pair<Satisfaction, CachePolicy>>, Provider<?>> providerCache;
    
    private final CachePolicy defaultPolicy;

    /**
     * <p>
     * Create a new DefaultInjector. The created resolver will use a max
     * dependency depth of 100 to estimate if there are cycles in the dependency
     * hierarchy. Bindings with a NO_PREFERENCE cache policy will be treated as
     * MEMOIZED.
     * 
     * @param spi The InjectSPI to use
     * @param functions The BindingFunctions to use, ordered with highest
     *            priority function first
     * @throws NullPointerException if spi or functions ar enull
     */
    public DefaultInjector(InjectSPI spi, BindingFunction... functions) {
        this(spi, CachePolicy.MEMOIZE, functions);
    }
    
    /**
     * <p>
     * Create a new DefaultInjector. The created resolver will use a max
     * dependency depth of 100 to estimate if there are cycles in the dependency
     * hierarchy. Bindings with a NO_PREFERENCE cache policy will use
     * <tt>defaultPolicy</tt>.
     * 
     * @param spi The InjectSPI to use
     * @param defaultPolicy The CachePolicy used in place of NO_PREFERENCE
     * @param functions The BindingFunctions to use, ordered with highest
     *            priority functions first
     * @throws IllegalArgumentException if defaultPolicy is NO_PREFERENCE
     * @throws NullPointerException if spi or functions are null
     */
    public DefaultInjector(InjectSPI spi, CachePolicy defaultPolicy, BindingFunction... functions) {
        this(spi, defaultPolicy, 100, functions);
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
     * @param spi The InjectSPI to use
     * @param defaultPolicy The CachePolicy used in place of NO_PREFERENCE
     * @param maxDepth The maximum depth of the dependency hierarchy
     * @param functions The BindingFunctions to use, ordered with highest
     *            priority functions first
     * @throws IllegalArgumentException if maxDepth is less than 1, or if
     *             defaultPolicy is NO_PREFERENCE
     * @throws NullPointerException if spi or functions are null
     */
    public DefaultInjector(InjectSPI spi, CachePolicy defaultPolicy, int maxDepth, BindingFunction... functions) {
        Preconditions.notNull("spi", spi);
        if (defaultPolicy.equals(CachePolicy.NO_PREFERENCE)) {
            throw new IllegalArgumentException("Default CachePolicy cannot be NO_PREFERENCE");
        }
        
        this.spi = spi;
        this.defaultPolicy = defaultPolicy;
        solver = new DependencySolver(Arrays.asList(functions), maxDepth);
        providerCache = new HashMap<Node<Pair<Satisfaction, CachePolicy>>, Provider<?>>();
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
        Desire desire = spi.desire(qualifier, type, false);
        
        // check if the desire is already in the graph
        Edge<Pair<Satisfaction, CachePolicy>, Desire> resolved = solver.getGraph().getOutgoingEdge(solver.getRootNode(), desire);
        
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
            resolved = solver.getGraph().getOutgoingEdge(solver.getRootNode(), desire);
        }
        
        // Check if the provider for the resolved node is in our cache
        Node<Pair<Satisfaction, CachePolicy>> resolvedNode = resolved.getTail();
        return (T) getProvider(resolvedNode).get();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Provider<?> getProvider(Node<Pair<Satisfaction, CachePolicy>> node) {
        Provider<?> cached = providerCache.get(node);
        if (cached == null) {
            logger.debug("Node has not been memoized, instantiating: {}", node.getLabel());
            Provider<?> raw = node.getLabel().getKey().makeProvider(new DesireProviderMapper(node));
            
            CachePolicy policy = node.getLabel().getValue();
            if (policy.equals(CachePolicy.NO_PREFERENCE)) {
                policy = defaultPolicy;
            }
            
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
    
    private class MemoizingProvider<T> implements Provider<T> {
        private final Provider<T> raw;
        private T cached;
        
        public MemoizingProvider(Provider<T> raw) {
            this.raw = raw;
        }
        
        @Override
        public T get() {
            if (cached == null) {
                cached = raw.get();
            }
            return cached;
        }
    }
    
    private class DesireProviderMapper implements ProviderSource {
        private final Node<Pair<Satisfaction, CachePolicy>> forNode;
        
        public DesireProviderMapper(Node<Pair<Satisfaction, CachePolicy>> forNode) {
            this.forNode = forNode;
        }
        
        @Override
        public Provider<?> apply(Desire desire) {
            Edge<Pair<Satisfaction, CachePolicy>, Desire> edge = solver.getGraph().getOutgoingEdge(forNode, desire);
            Node<Pair<Satisfaction, CachePolicy>> dependency = edge.getTail();
            return getProvider(dependency);
        }
    }
}
