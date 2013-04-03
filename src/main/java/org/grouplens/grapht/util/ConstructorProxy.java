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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Proxy class for serializing constructors
 */
public final class ConstructorProxy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ClassProxy declaringClass;
    private final ClassProxy[] parameterTypes;
    @Nullable
    private transient volatile Constructor constructor;
    private transient volatile int hash;
    @Nullable
    private transient volatile String stringRepr;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    private ConstructorProxy(ClassProxy cls, ClassProxy[] ptypes) {
        declaringClass = cls;
        parameterTypes = ptypes;
    }

    @Override
    public String toString() {
        if (stringRepr == null) {
            StringBuilder bld = new StringBuilder();
            bld.append("proxy of ")
               .append(declaringClass.getClassName())
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
            stringRepr = bld.toString();
        }
        return stringRepr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ConstructorProxy) {
            ConstructorProxy op = (ConstructorProxy) o;
            return declaringClass.equals(op.declaringClass)
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
                      .append(parameterTypes)
                      .hashCode();
        }
        return hash;
    }

    /**
     * Resolve this proxy into a {@link java.lang.reflect.Constructor} instance.
     * @return The {@link java.lang.reflect.Constructor} represented by this proxy.
     * @throws ClassNotFoundException If the proxy's declaring type cannot be resolved.
     * @throws NoSuchMethodException If the constructor does not exist on the declaring type.
     */
    public Constructor resolve() throws ClassNotFoundException, NoSuchMethodException {
        if (constructor == null) {
            Class<?> cls = declaringClass.resolve();
            Class<?>[] ptypes = new Class<?>[parameterTypes.length];
            for (int i = ptypes.length - 1; i >= 0; i--) {
                ptypes[i] = parameterTypes[i].resolve();
            }
            constructor = cls.getDeclaredConstructor(ptypes);
        }
        return constructor;
    }

    /**
     * Construct a proxy for a constructor.
     * @param constructor The constructor to proxy.
     * @return The constructor proxy representing {@code constructor}.
     */
    public static ConstructorProxy of(Constructor constructor) {
        Class<?>[] ptypes = constructor.getParameterTypes();
        ClassProxy[] proxies = new ClassProxy[ptypes.length];
        for (int i = ptypes.length - 1; i >= 0; i--) {
            proxies[i] = ClassProxy.of(ptypes[i]);
        }
        ConstructorProxy proxy =
                new ConstructorProxy(ClassProxy.of(constructor.getDeclaringClass()),
                                     proxies);
        proxy.constructor = constructor;
        return proxy;
    }
}
