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
import org.grouplens.grapht.spi.Desires;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.util.FieldProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * FieldInjectionPoint is an injection point wrapping a field.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class FieldInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;
    // transient because we use a serialization proxy
    private final transient Field field;
    private final transient Attributes attributes;
    
    /**
     * Create an injection point wrapping the given field
     * 
     * @param field The field to inject
     * @throws NullPointerException if field is null
     */
    public FieldInjectionPoint(@Nonnull Field field) {
        Preconditions.notNull("field", field);
        this.field = field;
        attributes = Desires.createAttributes(field.getAnnotations());
    }
    
    @Override
    public Type getType() {
        return Types.box(field.getGenericType());
    }

    @Override
    public Class<?> getErasedType() {
        return Types.box(field.getType());
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override @Nonnull
    public Field getMember() {
        return field;
    }

    @Override
    public boolean isNullable() {
        return Types.hasNullableAnnotation(field.getAnnotations());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FieldInjectionPoint)) {
            return false;
        }
        return ((FieldInjectionPoint) o).field.equals(field);
    }
    
    @Override
    public int hashCode() {
        return field.hashCode();
    }
    
    @Override
    public String toString() {
        return field.toString();
    }

    private Object writeReplace() {
        return new SerialProxy(field);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final FieldProxy field;

        public SerialProxy(Field f) {
            field = FieldProxy.of(f);
        }

        private Object readResolve() throws InvalidObjectException {
            try {
                return new FieldInjectionPoint(field.resolve());
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("no class for " + field.toString());
                ex.initCause(e);
                throw ex;
            } catch (NoSuchFieldException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve " + field.toString());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
