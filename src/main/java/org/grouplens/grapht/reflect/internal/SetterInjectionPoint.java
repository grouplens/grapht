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
import org.grouplens.grapht.reflect.InjectionPointVisitor;
import org.grouplens.grapht.util.MethodProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class SetterInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;
    // transient because we use a serialization proxy
    private final transient Method setter;
    private final transient int parameter;
    private final transient AnnotationHelper annotations;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     * @param parameter The parameter index to apply
     */
    public SetterInjectionPoint(@Nonnull Method setter, int parameter) {
        Preconditions.notNull("setter method", setter);
        Preconditions.inRange(parameter, 0, setter.getParameterTypes().length);
        
        this.annotations = new AnnotationHelper(setter.getParameterAnnotations()[parameter]);
        this.setter = setter;
        this.parameter = parameter;
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    @Override @Nonnull
    public Method getMember() {
        return setter;
    }
    
    /**
     * @return The parameter index of this injection point within the
     *         setter's parameters
     */
    public int getParameterIndex() {
        return parameter;
    }
    
    @Override
    public boolean isNullable() {
        // we'll check both setter and parameter annotations
        return Types.hasNullableAnnotation(setter.getAnnotations()) || 
               Types.hasNullableAnnotation(setter.getParameterAnnotations()[parameter]);
    }

    @Override
	public <X extends Exception>  void accept(InjectionPointVisitor <X> visitor) throws X {
        visitor.visitSetter(this);
	}

    @Override
    public Type getType() {
        return Types.box(setter.getGenericParameterTypes()[parameter]);
    }
    
    @Override
    public Class<?> getErasedType() {
        return Types.box(setter.getParameterTypes()[parameter]);
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
        if (!(o instanceof SetterInjectionPoint)) {
            return false;
        }
        SetterInjectionPoint p = (SetterInjectionPoint) o;
        return p.setter.equals(setter) && p.parameter == parameter;
    }
    
    @Override
    public int hashCode() {
        return setter.hashCode() ^ (37 * 17 * parameter);
    }
    
    @Override
    public String toString() {
        // method setFoo(..., @Qual Type argN, ...)
        StringBuilder sb = new StringBuilder();
        sb.append("method ")
          .append(setter.getName())
          .append("(");
        if (parameter > 0) {
            sb.append("..., ");
        }
        if (annotations.getQualifier() != null) {
            sb.append(annotations.getQualifier())
              .append(" ");
        }
        sb.append(setter.getParameterTypes()[parameter].getName())
          .append(" arg")
          .append(parameter);
        if (parameter < setter.getParameterTypes().length) {
            sb.append(", ...");
        }
        sb.append(")");

        return sb.toString();
    }
    
    private Object writeReplace() {
        return new SerialProxy(setter, parameter);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final MethodProxy method;
        private final int parameterIndex;
        public SerialProxy(Method m, int pidx) {
            method = MethodProxy.of(m);
            parameterIndex = pidx;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new SetterInjectionPoint(method.resolve(), parameterIndex);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("no class for " + method.toString());
                ex.initCause(e);
                throw ex;
            } catch (NoSuchMethodException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve " + method.toString());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}