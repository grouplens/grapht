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

import java.lang.reflect.Method;

import org.grouplens.grapht.util.Types;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class SetterInjectionPoint implements InjectionPoint {
    private final Method setter;
    private final int parameter;
    private final AnnotationQualifier qualifier;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     */
    public SetterInjectionPoint(Method setter, int parameter) {
        Checks.notNull("setter method", setter);
        Checks.inRange(parameter, 0, setter.getParameterTypes().length);
        
        this.qualifier = Qualifiers.getQualifier(setter.getParameterAnnotations()[parameter]);
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
    public boolean isTransient() {
        return Types.hasTransientAnnotation(setter.getAnnotations()) ||
               Types.hasTransientAnnotation(setter.getParameterAnnotations()[parameter]);
    }

    @Override
    public Class<?> getType() {
        return Types.box(setter.getParameterTypes()[parameter]);
    }

    @Override
    public AnnotationQualifier getQualifier() {
        return qualifier;
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
        String q = (qualifier == null ? "" : qualifier + ":");
        String p = setter.getParameterTypes()[parameter].getSimpleName();
        return setter.getName() + "(" + parameter + ", " + q + p + ")";
    }
}
