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
 * Abstract implementation of {@link SatisfactionVisitor}.  All methods delegate to
 * {@link #visitDefault()}, which in turn returns a default value.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @since 0.5
 */
public abstract class AbstractSatisfactionVisitor<T> implements SatisfactionVisitor<T> {
    protected final T defaultValue;

    /**
     * Construct a visitor with a null default.
     */
    public AbstractSatisfactionVisitor() {
        this(null);
    }

    /**
     * Construct a visitor with the specified default value.
     *
     * @param dft The default value to be returned by {@link #visitDefault()}.
     */
    public AbstractSatisfactionVisitor(T dft) {
        defaultValue = dft;
    }

    /**
     * Default method called when other methods are not overridden.  The default implementation
     * returns the default value provided to {@link #AbstractSatisfactionVisitor(Object)}.
     * @return The return value.
     */
    public T visitDefault() {
        return defaultValue;
    }

    @Override
    public T visitNull() {
        return visitDefault();
    }

    @Override
    public T visitClass(Class<?> clazz) {
        return visitDefault();
    }

    @Override
    public T visitInstance(Object instance) {
        return visitDefault();
    }

    @Override
    public T visitProviderClass(Class<? extends Provider<?>> pclass) {
        return visitDefault();
    }

    @Override
    public T visitProviderInstance(Provider<?> provider) {
        return visitDefault();
    }
}
