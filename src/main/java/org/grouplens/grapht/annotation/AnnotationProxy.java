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
package org.grouplens.grapht.annotation;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.lang3.ClassUtils;
import org.grouplens.grapht.util.ClassProxy;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * Proxy used to implement annotation interfaces.  It implements the {@link Annotation}
 * contract by delegating to a map of named attribute values.  A new AnnotationProxy instance
 * should be created for each proxy annotation.
 *
 * @see AnnotationBuilder
 */
class AnnotationProxy<T extends Annotation> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1L;
    private final ClassProxy annotationType;
    private final ImmutableMap<String, Object> attributes;
    private transient Class<T> cachedType;

    public AnnotationProxy(Class<T> type, Map<String, Object> attrs) {
        annotationType = ClassProxy.of(type);
        cachedType = type;
        attributes = ImmutableMap.copyOf(attrs);
    }

    /**
     * Customized {@code readObject} implementation to ensure the cached type is resolved.
     *
     * @param in The stream.
     * @throws java.io.ObjectStreamException If there is an error reading the object from the stream.
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws ObjectStreamException {
        try {
            in.defaultReadObject();
            cachedType = (Class<T>) annotationType.resolve();
        } catch (IOException e) {
            ObjectStreamException ex = new StreamCorruptedException("IO exception");
            ex.initCause(e);
            throw ex;
        } catch (ClassNotFoundException e) {
            ObjectStreamException ex = new InvalidObjectException("IO exception");
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isHashCode(method)) {
            return proxyHashCode(proxy);
        } else if (isEquals(method)) {
            return proxyEquals(proxy, args[0]);
        } else if (isAnnotationType(method)) {
            return proxyAnnotationType();
        } else if (isToString(method)) {
            return proxyToString(proxy);
        } else if (attributes.containsKey(method.getName()) && method.getParameterTypes().length == 0) {
            return copyAnnotationValue(attributes.get(method.getName()));
        } else {
            // fall back to the default
            return copyAnnotationValue(method.getDefaultValue());
        }
        // wait() and other Object methods do not get sent to the InvocationHandler
        // so we don't have any other cases
    }

    private boolean isEquals(Method m) {
        return m.getName().equals("equals") && m.getReturnType().equals(boolean.class)
            && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(Object.class);
    }

    private boolean isHashCode(Method m) {
        return m.getName().equals("hashCode") && m.getReturnType().equals(int.class)
            && m.getParameterTypes().length == 0;
    }

    private boolean isAnnotationType(Method m) {
        return m.getName().equals("annotationType") && m.getReturnType().equals(Class.class)
            && m.getParameterTypes().length == 0;
    }

    private boolean isToString(Method m) {
        return m.getName().equals("toString") && m.getReturnType().equals(String.class)
            && m.getParameterTypes().length == 0;
    }

    private Class<? extends Annotation> proxyAnnotationType() {
        return cachedType;
    }

    private String proxyToString(Object o) {
        return AnnotationUtils.toString((Annotation) o);
    }

    private int proxyHashCode(Object proxy) {
        return AnnotationUtils.hashCode((Annotation) proxy);
    }

    private boolean proxyEquals(Object o1, Object o2) {
        return AnnotationUtils.equals((Annotation) o1, (Annotation) o2);
    }

    /**
     * Safe clone of an object.  If the object is an array, it is copied; otherwise, it is
     * returned as-is.  This object is only applicable to valid annotation value types, which
     * are all either arrays or immutable.
     * @param o The annotation value.
     * @return A copy of the value.
     */
    @SuppressWarnings("unchecked")
    static Object copyAnnotationValue(Object o) {
        if (o.getClass().isArray()) {
            // make a shallow copy of the array
            if (o instanceof boolean[]) {
                boolean[] a = (boolean[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof byte[]) {
                byte[] a = (byte[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof short[]) {
                short[] a = (short[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof int[]) {
                int[] a = (int[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof long[]) {
                long[] a = (long[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof char[]) {
                char[] a = (char[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof float[]) {
                float[] a = (float[]) o;
                return Arrays.copyOf(a, a.length);
            } else if (o instanceof double[]) {
                double[] a = (double[]) o;
                return Arrays.copyOf(a, a.length);
            } else {
                Object[] a = (Object[]) o;
                return Arrays.copyOf(a, a.length, (Class<? extends Object[]>) o.getClass());
            }
        } else if (o instanceof String
                   || o instanceof Annotation
                   || ClassUtils.isPrimitiveOrWrapper(o.getClass())
                   || o instanceof Enum
                   || o instanceof Class) {
            // the value is immutable and a copy is not necessary
            return o;
        } else {
            throw new IllegalArgumentException("not an annotation value");
        }
    }
}
