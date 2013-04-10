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

import javax.inject.Provider;

/**
 * Abstract implementation of {@link SatisfactionVisitor}.  All methods delegate to
 * {@link #visitDefault()}, which in turn returns a default value.
 *
 * @author Michael Ekstrand
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
