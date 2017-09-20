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

import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.jetbrains.annotations.Nullable;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Proxy class for serializing constructors
 */
public final class ConstructorProxy implements MemberProxy {
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
        String repr = stringRepr;
        if (repr == null) {
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
            stringRepr = repr = bld.toString();
        }
        return repr;
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
    @Override
    public Constructor resolve() throws ClassNotFoundException, NoSuchMethodException {
        Constructor ctor = constructor;
        if (ctor == null) {
            Class<?> cls = declaringClass.resolve();
            Class<?>[] ptypes = new Class<?>[parameterTypes.length];
            for (int i = ptypes.length - 1; i >= 0; i--) {
                ptypes[i] = parameterTypes[i].resolve();
            }
            constructor = ctor = cls.getDeclaredConstructor(ptypes);
        }
        return ctor;
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
