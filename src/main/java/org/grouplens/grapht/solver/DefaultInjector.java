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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.InjectorConfiguration;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.Satisfaction;

/**
 * <p>
 * DefaultInjector is the default Injector implementation. When resolving the
 * dependency graph for a desire, a "context" is built which consists of an
 * ordering of the nodes and their {@link Qualifier}s which satisfy each dependency. For more
 * details, see {@link ContextChain} and {@link ContextMatcher}. The
 * DefaultInjector uses the context to activate and select BindRules. A number
 * of rules are used to order applicable BindRules and choose the best. When any
 * of these rules rely on the current dependency context, the deepest node in
 * the context has the most influence. Put another way, if contexts were
 * strings, they could be ordered lexicographically from the right to the left.
 * <p>
 * When selecting BindRules to apply to a Desire, BindRules are ordered by the
 * following rules:
 * <ol>
 * <li>Context closeness - BindRules with a context matching chain closer to the
 * leaf nodes of the current dependency context are selected.</li>
 * <li>Context chain length - BindRules with a longer context chain are
 * selected.</li>
 * <li>Context chain type delta - BindRules are ordered by how close their
 * context matching chain is to the current dependency context, as determined by
 * {@link Node#contextComparator(Qualifier)}.</li>
 * <li>Bind rule type delta - BindRules are lastly ordered by how well their
 * type matches a particular desire, as determined by
 * {@link Desire#ruleComparator()}.</li>
 * </ol>
 * <p>
 * A summary of these rules is that the best specified BindRule is applied,
 * where the context that the BindRule is activated in has more priority than
 * the type of the BindRule.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class DefaultInjector implements Injector {
    private final InjectorConfiguration config;
    private final DependencySolver solver;
    
    private final Map<Node<Satisfaction>, Provider<?>> providerCache;

    /**
     * <p>
     * Create a new DefaultInjector. The created resolver will use a max
     * dependency depth of 100 to estimate if there are cycles in the dependency
     * hierarchy.
     * 
     * @throws NullPointerException if config is null
     */
    public DefaultInjector(InjectorConfiguration config) {
        this(config, 100);
    }

    /**
     * <p>
     * Create a new DefaultInjector. <tt>maxDepth</tt> represents the maximum
     * depth of the dependency hierarchy before it is assume that there is a
     * cycle. This constructor can be used to increase this depth in the event
     * that configuration requires it, although for most purposes the default
     * 100 should be sufficient.
     * 
     * @param maxDepth The maximum depth of the dependency hierarchy
     * @throws IllegalArgumentException if maxDepth is less than 1
     * @throws NullPointerException if config is null
     */
    public DefaultInjector(InjectorConfiguration config, int maxDepth) {
        this.config = config;
        solver = new DependencySolver(config, maxDepth);
        providerCache = new HashMap<Node<Satisfaction>, Provider<?>>();
    }
    
    /**
     * @return The DependencySolver backing this injector
     */
    public DependencySolver getSolver() {
        return solver;
    }
    
    @Override
    public <T> T getInstance(Class<T> type) {
        return getInstance((Qualifier) null, type);
    }
    
    @Override
    public <T> T getInstance(Annotation qualifier, Class<T> type) {
        return getInstance(config.getSPI().qualifier(qualifier), type);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getInstance(@Nullable Qualifier q, Class<T> type) {
        Desire desire = config.getSPI().desire(q, type, false);
        
        // check if the desire is already in the graph
        Edge<Satisfaction, Desire> resolved = solver.getGraph().getOutgoingEdge(solver.getRootNode(), desire);
        
        // The edge is only non-null if getInstance() has been called before,
        // it may be present in the graph at a deeper node. If that's the case
        // it will be properly merged after regenerating the graph at the root context.
        if (resolved == null) {
            try {
                solver.resolve(desire);
            } catch(ResolverException e) {
                throw new InjectionException(type, null, e);
            }
            resolved = solver.getGraph().getOutgoingEdge(solver.getRootNode(), desire);
        }
        
        // Check if the provider for the resolved node is in our cache
        Node<Satisfaction> resolvedNode = resolved.getTail();
        return (T) getProvider(resolvedNode).get();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Provider<?> getProvider(Node<Satisfaction> node) {
        Provider<?> cached = providerCache.get(node);
        if (cached == null) {
            Provider<?> raw = node.getLabel().makeProvider(new DesireProviderMapper(node));
            cached = new MemoizingProvider(raw);
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
        private final Node<Satisfaction> forNode;
        
        public DesireProviderMapper(Node<Satisfaction> forNode) {
            this.forNode = forNode;
        }
        
        @Override
        public Provider<?> apply(Desire desire) {
            Edge<Satisfaction, Desire> edge = solver.getGraph().getOutgoingEdge(forNode, desire);
            Node<Satisfaction> dependency = edge.getTail();
            return getProvider(dependency);
        }
    }
}
