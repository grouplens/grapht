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
package org.grouplens.grapht.reflect;

import javax.inject.Provider;

/**
 * An interface for visiting {@link Satisfaction}s.
 *
 * @param <T> The type returned from this satisfaction's values.
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @since 0.5
 * @see Satisfaction
 */
public interface SatisfactionVisitor<T> {
    /**
     * Called when visiting a null satisfaction.
     *
     * @return The return value.
     */
    T visitNull();

    /**
     * Called when visiting a satisfaction that will instantiate a class.
     *
     * @param clazz The implementation class.
     * @return The return value.
     */
    T visitClass(Class<?> clazz);

    /**
     * Called when visiting a satisfaction that will return a pre-configured instance.
     *
     * @param instance The instance that will be returned.  The visitor should not modify it
     *                 in any way.
     * @return The return value.
     */
    T visitInstance(Object instance);

    /**
     * Called when visiting a satisfaction that will instantiate and invoke a provider class.
     *
     * @param pclass The provider class.
     * @return The return value.
     */
    T visitProviderClass(Class<? extends Provider<?>> pclass);

    /**
     * Called when visiting a satisfaction that will invoke a pre-instantiated provider.
     *
     * @param provider The provider instance.  The visitor should not modify it in any way.
     * @return The return value.
     */
    T visitProviderInstance(Provider<?> provider);
}
