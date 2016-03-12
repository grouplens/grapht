/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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

import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * InstanceProvider is a simple Provider that always provides the same instance.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @param <T>
 */
public class InstanceProvider<T> implements TypedProvider<T>, Serializable {
    private static final long serialVersionUID = -1L;

    private final Class<?> providedType;
    private final transient T instance; // transient because serialization proxy takes care of it

    /**
     * Construct a new instance provider.
     * @param instance The instance.
     * @deprecated Use {@link Providers#of(Object)} instead.
     */
    @Deprecated
    public InstanceProvider(T instance) {
        this(instance, instance == null ? Object.class : instance.getClass());
    }

    InstanceProvider(@Nullable T instance, Class<?> type) {
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

    @Override
    public String toString() {
        if (instance == null) {
            return "InstanceProvider{null<" + providedType.getName() + ">}";
        } else {
            return "InstanceProvider{" + instance + "}";
        }
    }

    private Object writeReplace() {
        return new SerialProxy(providedType,  instance);
    }

    private Object readObject() throws ObjectStreamException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private ClassProxy type;
        @SuppressWarnings("squid:S1948") // serializable warning; node is serializable iff its label type is
        private Object instance;

        public SerialProxy(Class<?> t, Object i) {
            type = ClassProxy.of(t);
            instance = i;
        }

        @SuppressWarnings("unchecked")
        private Object readResolve() throws ObjectStreamException {
            Class<?> cls = null;
            try {
                cls = type.resolve();
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("class not found");
                ex.initCause(e);
                throw ex;
            }
            if (instance != null && !cls.isInstance(instance)) {
                throw new InvalidObjectException("instance is not of expected type");
            }
            return new InstanceProvider(instance, cls);
        }
    }
}
