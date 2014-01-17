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

import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.ConstructorProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * ConstructorParameterInjectionPoint is an injection point wrapping a parameter
 * of a constructor.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ConstructorParameterInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;

    // transient because of serialization proxy
    private final transient Constructor<?> constructor;
    private final transient int paramIndex;
    private final transient AnnotationHelper annotations;

    /**
     * Create a ConstructorParameterInjectionPoint that wraps the given parameter index for the
     * given constructor, ctor.
     *
     * @param ctor   The constructor to wrap
     * @param pIndex The parameter index of this injection point within ctor's parameters
     * @throws NullPointerException      if {@code ctor} is null
     * @throws IndexOutOfBoundsException if {@code pIndex} is not a valid index into the
     *                                   constructor's parameters
     */
    public ConstructorParameterInjectionPoint(@Nonnull Constructor<?> ctor, int pIndex) {
        Preconditions.notNull("constructor", ctor);
        Preconditions.inRange(pIndex, 0, ctor.getParameterTypes().length);

        constructor = ctor;
        paramIndex = pIndex;
        annotations = new AnnotationHelper(ctor.getParameterAnnotations()[pIndex]);
    }

    /**
     * @return The constructor wrapped by this injection point
     */
    @Override @Nonnull
    public Constructor<?> getMember() {
        return constructor;
    }
    
    /**
     * @return The parameter index of this injection point within the
     *         constructor's parameters
     */
    public int getParameterIndex() {
        return paramIndex;
    }
    
    @Override
    public boolean isNullable() {
        return Types.hasNullableAnnotation(constructor.getParameterAnnotations()[paramIndex]);
    }
    
    @Override
    public Type getType() {
        return Types.box(constructor.getGenericParameterTypes()[paramIndex]);
    }

    @Override
    public Class<?> getErasedType() {
        return Types.box(constructor.getParameterTypes()[paramIndex]);
    }

    @Nullable
    @Override
    public Annotation getQualifier() {
        return annotations.getQualifier();
    }

    @Nullable
    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return annotations.getAttribute(atype);
    }

    @Nonnull
    @Override
    public Collection<Annotation> getAttributes() {
        return annotations.getAttributes();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorParameterInjectionPoint)) {
            return false;
        }
        ConstructorParameterInjectionPoint cp = (ConstructorParameterInjectionPoint) o;
        return cp.constructor.equals(constructor) && cp.paramIndex == paramIndex;
    }
    
    @Override
    public int hashCode() {
        return constructor.hashCode() ^ (37 * 17 * paramIndex);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String p = constructor.getParameterTypes()[paramIndex].getSimpleName();
        sb.append(constructor.getDeclaringClass().getSimpleName())
          .append("(")
          .append(paramIndex)
          .append(", ");
        if (annotations.getQualifier() != null) {
            sb.append(annotations.getQualifier())
              .append(":");
        }
        return sb.append(p)
                 .append(")")
                 .toString();
    }

    private Object writeReplace() {
        return new SerialProxy(constructor, paramIndex);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ConstructorProxy constructor;
        private final int index;

        public SerialProxy(Constructor ctor, int idx) {
            constructor = ConstructorProxy.of(ctor);
            index = idx;
        }

        private Object readResolve() throws InvalidObjectException {
            try {
                return new ConstructorParameterInjectionPoint(constructor.resolve(), index);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("no class for " + constructor.toString());
                ex.initCause(e);
                throw ex;
            } catch (NoSuchMethodException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve " + constructor.toString());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
