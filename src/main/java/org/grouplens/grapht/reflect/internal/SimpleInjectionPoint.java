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
package org.grouplens.grapht.reflect.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.util.Collection;
import java.util.Collections;

/**
 * Synthetic injection point used for {@link Desires#create(java.lang.annotation.Annotation, Class, boolean)}.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */

public final class SimpleInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;
    // fields marked as transient since direct serialization is disabled
    private final transient Annotation qualifier;
    private final transient Class<?> type;
    private final transient boolean nullable;

    public SimpleInjectionPoint(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        Preconditions.notNull("type", type);
        if (qualifier != null) {
            Preconditions.isQualifier(qualifier.annotationType());
        }
        this.qualifier = qualifier;
        this.type = type;
        this.nullable = nullable;
    }

    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Override
    public Member getMember() {
        return null;
    }

    @Override
    public boolean isOptional() {
        return nullable;
    }

    @Override
    public Class<?> getType() {
        return type;
    }

    @Nullable
    @Override
    public Annotation getQualifier() {
        return qualifier;
    }

    @Nullable
    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return null;
    }

    @NotNull
    @Override
    public Collection<Annotation> getAttributes() {
        return Collections.emptyList();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(qualifier).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleInjectionPoint)) {
            return false;
        }
        SimpleInjectionPoint p = (SimpleInjectionPoint) o;
        EqualsBuilder eqb = new EqualsBuilder();
        return eqb.append(type, p.type)
                  .append(qualifier, p.qualifier)
                  .append(nullable, p.nullable)
                  .isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("synthetic ");
        if (qualifier != null) {
            sb.append(qualifier)
              .append(" ");
        }
        return sb.append(type.getName()).toString();
    }

    private Object writeReplace() {
        return new SerialProxy(type, nullable, qualifier);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    /**
     * Serialization proxy for the Serialization Proxy Pattern.
     */
    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private ClassProxy type;
        private boolean nullable;
        @Nullable
        @SuppressWarnings("squid:S1948") // serializable - annotations are serializable
        private Annotation qualifier;

        private SerialProxy(Class<?> t, boolean isNullable, @Nullable Annotation qual) {
            type = ClassProxy.of(t);
            nullable = isNullable;
            qualifier = qual;
        }

        public Object readResolve() throws ObjectStreamException {
            try {
                return Desires.createInjectionPoint(qualifier, type.resolve(), nullable);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve class " + type.getClassName());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
