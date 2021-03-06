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

import net.jcip.annotations.ThreadSafe;
import org.grouplens.grapht.*;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Desires;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;

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
    private final InjectionContainer instantiator;
    private final LifecycleManager manager;

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
                                 .setMaxDepth(maxDepth)
                                 .build();
        manager = new LifecycleManager();
        instantiator = InjectionContainer.create(defaultPolicy, manager);
    }
    
    /**
     * @return The DependencySolver backing this injector
     */
    public DependencySolver getSolver() {
        return solver;
    }
    
    @NotNull
    @Override
    public <T> T getInstance(Class<T> type) throws InjectionException {
        return getInstance(null, type);
    }
    
    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public <T> T getInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        Object obj = getInstance(Desires.create(qualifier, type, false));
        assert obj != null;
        return type.cast(obj);
    }

    @Nullable
    @Override
    public <T> T tryGetInstance(Annotation qualifier, Class<T> type) throws InjectionException {
        Object obj = getInstance(Desires.create(qualifier, type, true));
        return type.cast(obj);
    }

    private Object getInstance(Desire desire) throws InjectionException {
        // All Provider cache access, graph resolution, etc. occur
        // within this exclusive lock so we know everything is thread safe
        // albeit in a non-optimal way.
        synchronized(this) {
            // check if the desire is already in the graph
            DAGEdge<Component, Dependency> resolved =
                    solver.getGraph()
                          .getOutgoingEdgeWithLabel(d -> d.hasInitialDesire(desire));

            // The edge is only non-null if instantiate() has been called before,
            // it may be present in the graph at a deeper node. If that's the case
            // it will be properly merged after regenerating the graph at the root context.
            if (resolved == null) {
                logger.info("Must resolve desire: {}", desire);
                solver.resolve(desire);
                resolved = solver.getGraph()
                                 .getOutgoingEdgeWithLabel(d -> d.hasInitialDesire(desire));
            }

            // Check if the provider for the resolved node is in our cache
            DAGNode<Component, Dependency> resolvedNode = resolved.getTail();
            return instantiator.makeInstantiator(resolvedNode, solver.getBackEdges()).instantiate();
        }
    }

    @Override
    public void close() {
        if (manager != null) {
            manager.close();
        }
    }
}
