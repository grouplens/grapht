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
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.util.MethodProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class SetterInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = 1L;
    // transient because we use a serialization proxy
    private final transient Method setter;
    private final transient int parameter;
    private final transient Attributes attributes;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     * @param parameter The parameter index to apply
     */
    public SetterInjectionPoint(Method setter, int parameter) {
        Preconditions.notNull("setter method", setter);
        Preconditions.inRange(parameter, 0, setter.getParameterTypes().length);
        
        this.attributes = new AttributesImpl(setter.getParameterAnnotations()[parameter]);
        this.setter = setter;
        this.parameter = parameter;
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    @Override
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
    public Type getType() {
        return Types.box(setter.getGenericParameterTypes()[parameter]);
    }
    
    @Override
    public Class<?> getErasedType() {
        return Types.box(setter.getParameterTypes()[parameter]);
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
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
        String q = (attributes.getQualifier() == null ? "" : attributes.getQualifier() + ":");
        String p = setter.getParameterTypes()[parameter].getSimpleName();
        return setter.getName() + "(" + parameter + ", " + q + p + ")";
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
            method = MethodProxy.forMethod(m);
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
