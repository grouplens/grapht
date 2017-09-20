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
