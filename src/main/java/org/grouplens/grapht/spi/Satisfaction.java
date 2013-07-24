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
package org.grouplens.grapht.spi;

import org.grouplens.grapht.Injector;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.List;


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
    List<? extends Desire> getDependencies();

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
     * Create a provider from this satisfaction.
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
     * @return A provider of new instances of the type specified by this
     *         satisfaction, instantiated using the specified dependency
     *         mapping.
     */
    Provider<?> makeProvider(ProviderSource dependencies);
}
