/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Method;

import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.util.Types;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class SetterInjectionPoint implements InjectionPoint {
    private final Method setter;
    private final int parameter;
    private final Qualifier qualifier;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     */
    public SetterInjectionPoint(Method setter, int parameter) {
        if (setter == null) {
            throw new NullPointerException("Setter method cannot null");
        }
        
        int numArgs = setter.getParameterTypes().length;
        if (parameter < 0 || parameter >= numArgs) {
            throw new IndexOutOfBoundsException("Setter parameter is invalid");
        }
        
        this.qualifier = Qualifiers.getQualifier(setter.getParameterAnnotations()[parameter]);
        this.setter = setter;
        this.parameter = parameter;
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    public Method getSetterMethod() {
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
    public Qualifier getQualifier() {
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
        return "Setter(method=" + setter + "param=" + parameter + ")";
    }
}
