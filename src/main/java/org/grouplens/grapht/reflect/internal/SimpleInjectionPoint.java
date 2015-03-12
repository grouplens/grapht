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
package org.grouplens.grapht.reflect.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.InjectionPointVisitor;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public void accept(InjectionPointVisitor visitor) throws InjectionException {
        visitor.visitSynthetic(this);
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

    @Nonnull
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
        @Nullable @SuppressWarnings("SE_BAD_FIELD")
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
