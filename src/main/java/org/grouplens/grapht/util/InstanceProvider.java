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
package org.grouplens.grapht.util;

/**
 * InstanceProvider is a simple Provider that always provides the same instance.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T>
 */
public class InstanceProvider<T> implements TypedProvider<T> {
    private final Class<?> providedType;
    private final T instance;

    /**
     * Construct a new instance provider.
     * @param instance The instance.
     * @deprecated Use {@link Providers#of(Object)} instead.
     */
    @Deprecated
    public InstanceProvider(T instance) {
        this(instance, instance == null ? Object.class : instance.getClass());
    }

    InstanceProvider(T instance, Class<?> type) {
        if (instance != null && !type.isInstance(instance)) {
            throw new IllegalArgumentException("instance not of specified type");
        }
        this.instance = instance;
        providedType = type;
    }

    @Override
    public Class<?> getProvidedType() {
        return providedType;
    }

    @Override
    public T get() {
        return instance;
    }
}
