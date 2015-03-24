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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Proxy class for serializing methods
 */
public final class MethodProxy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ClassProxy declaringClass;
    private final String methodName;
    private final ClassProxy[] parameterTypes;
    @Nullable
    private transient volatile Method method;
    private transient volatile int hash;
    @Nullable
    private transient volatile String stringRepr;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    private MethodProxy(ClassProxy cls, String names, ClassProxy[] ptypes) {
        declaringClass = cls;
        methodName = names;
        parameterTypes = ptypes;
    }

    @Override
    public String toString() {
        String repr = stringRepr;
        if (repr == null) {
            StringBuilder bld = new StringBuilder();
            bld.append("proxy of ")
               .append(declaringClass.getClassName())
               .append(".")
               .append(methodName)
               .append("(");
            boolean first = true;
            for (ClassProxy p: parameterTypes) {
                if (!first) {
                    bld.append(", ");
                }
                bld.append(p.getClassName());
                first = false;
            }
            bld.append(")");
            stringRepr = repr = bld.toString();
        }
        return repr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof MethodProxy) {
            MethodProxy op = (MethodProxy) o;
            return declaringClass.equals(op.declaringClass)
                    && methodName.equals(op.methodName)
                    && Arrays.deepEquals(parameterTypes, op.parameterTypes);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hash = hcb.append(declaringClass)
                      .append(methodName)
                      .append(parameterTypes)
                      .hashCode();
        }
        return hash;
    }

    /**
     * Resolve this proxy into a {@link java.lang.reflect.Method} instance.
     * @return The {@link java.lang.reflect.Method} represented by this proxy.
     * @throws ClassNotFoundException If the proxy's declaring type cannot be resolved.
     * @throws NoSuchMethodException If the method does not exist on the declaring type.
     */
    public Method resolve() throws ClassNotFoundException, NoSuchMethodException {
        Method m = method;
        if (m == null) {
            Class<?> cls = declaringClass.resolve();
            Class<?>[] ptypes = new Class<?>[parameterTypes.length];
            for (int i = ptypes.length - 1; i >= 0; i--) {
                ptypes[i] = parameterTypes[i].resolve();
            }
            method = m = cls.getDeclaredMethod(methodName, ptypes);
        }
        return m;
    }

    /**
     * Construct a proxy for a method.
     * @param method The method to proxy.
     * @return The method proxy representing {@code method}.
     */
    public static MethodProxy of(Method method) {
        Class<?>[] ptypes = method.getParameterTypes();
        ClassProxy[] proxies = new ClassProxy[ptypes.length];
        for (int i = ptypes.length - 1; i >= 0; i--) {
            proxies[i] = ClassProxy.of(ptypes[i]);
        }
        MethodProxy proxy = new MethodProxy(ClassProxy.of(method.getDeclaringClass()),
                                          method.getName(), proxies);
        proxy.method = method;
        return proxy;
    }
}
