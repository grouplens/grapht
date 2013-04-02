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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nullable;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

/**
 * SimpleInjectionPoint is a synthetic injection point used for
 * {@link InjectSPI#desire(Annotation, Class, boolean)}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class SimpleInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = 1L;
    // fields marked as transient since direct serialization is disabled
    private transient final Attributes attrs;
    private transient final Class<?> type;
    private transient final boolean nullable;
    
    public SimpleInjectionPoint(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        Preconditions.notNull("type", type);
        if (qualifier != null) {
            Preconditions.isQualifier(qualifier.annotationType());
        }

        this.attrs = (qualifier == null ? new AttributesImpl() : new AttributesImpl(qualifier));
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
    public Class<?> getType() {
        return type;
    }

    @Override
    public Attributes getAttributes() {
        return attrs;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ attrs.hashCode();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleInjectionPoint)) {
            return false;
        }
        SimpleInjectionPoint p = (SimpleInjectionPoint) o;
        return p.type.equals(type) && p.attrs.equals(attrs) && p.nullable == nullable;
    }
    
    @Override
    public String toString() {
        String q = (attrs.getQualifier() == null ? "" : attrs.getQualifier() + ":");
        return q + type.getSimpleName();
    }

    private Object writeReplace() {
        // REVIEW Why do we only write the qualifier?
        return new SerialProxy(type, nullable, attrs.getQualifier());
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
        private Annotation qualifier;

        private SerialProxy(Class<?> t, boolean isNullable, @Nullable Annotation qual) {
            type = ClassProxy.forClass(t);
            nullable = isNullable;
            qualifier = qual;
        }

        public SerialProxy() {}

        public Object readResolve() throws ObjectStreamException {
            try {
                return new SimpleInjectionPoint(qualifier, type.resolve(), nullable);
            } catch (ClassNotFoundException e) {
                throw new InvalidObjectException("cannot resolve class " + type.getClassName());
            }
        }
    }
}