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
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.LifecycleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


/**
 * A concrete type. It has a set of dependencies which must be satisfied in
 * order to instantiate it. It can also be viewed as an instantiable extension
 * of {@link Type}.
 * <p>
 * Satisfactions are expected to provide a reasonable implementation of
 * {@link Object#equals(Object)} and {@link Object#hashCode()} so that they can
 * be de-duplicated, etc.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface Satisfaction extends Serializable {
    /**
     * Get this satisfaction's dependencies.
     *
     * @return A list of dependencies which must be satisfied in order to
     *         instantiate this satisfaction.
     */
    List<Desire> getDependencies();

    /**
     * Get the type of this satisfaction. If this is a synthetic Satisfaction,
     * then a null type is returned.
     * 
     * @return The type of objects to be instantiated by this satisfaction.
     */
    Type getType();

    /**
     * Get the type-erased class of this satisfaction's type. If this is a
     * synthetic Satisfaction, then null is returned.
     * 
     * @return The class object for this satisfaction's type.
     */
    Class<?> getErasedType();

    /**
     * Query whether this satisfaction already has an instance to return. If
     * true, then this satisfaction's provider will just return an instance
     * that is already in existence; if false, then its provider may create
     * new instances.
     *
     * @return whether the satisfaction already has an instance.
     */
    boolean hasInstance();

    /**
     * Visit the satisfaction. This method invokes the appropriate method on the
     * provided visitor to report information on itself.
     *
     * @param visitor The visitor object.
     * @param <T> The type returned from the visitor.
     * @return The return value from the invoked visitor method.
     */
    <T> T visit(SatisfactionVisitor<T> visitor);
    
    /**
     * Get the default cache policy for instances created by this satisfaction.
     * In most cases this should be NO_PREFERENCE, but annotations such as
     * {@link Singleton} can be used to specify a default. BindingFunctions are
     * allowed to overrule the default cache policy.
     * 
     * @return The default cache policy if no function overrules it
     */
    CachePolicy getDefaultCachePolicy();

    /**
     * <p>
     * Create an instantiator from this satisfaction.
     * <p>
     * If the details of the Satisfaction require instantiation (e.g. a class or
     * provider class satisfaction), the returned provider should be a new
     * instance, and it should perform no memoization of its own. Caching is the
     * purview of {@link Injector} implementations.
     * <p>
     * If the satisfaction is configured to use specific instances, this rule is
     * obviously void.
     * 
     * @param dependencies A function mapping desires to providers of their
     *            instances.
     * @param lm The lifecycle manager (if one should be used).
     * @return An instantiator of new instances of the type specified by this
     *         satisfaction, instantiated using the specified dependency
     *         mapping.
     */
    Instantiator makeInstantiator(@NotNull Map<Desire,Instantiator> dependencies,
                                  @Nullable LifecycleManager lm);
}
