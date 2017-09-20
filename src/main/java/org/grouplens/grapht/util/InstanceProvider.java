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
package org.grouplens.grapht.util;

import org.jetbrains.annotations.Nullable;
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
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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
            Class<?> cls;
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
