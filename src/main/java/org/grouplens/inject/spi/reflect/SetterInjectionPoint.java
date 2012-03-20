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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.grouplens.inject.annotation.Transient;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.types.Types;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class SetterInjectionPoint implements InjectionPoint {
    private final Method setter;
    private final Qualifier qualifier;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     */
    public SetterInjectionPoint(Method setter) {
        if (setter == null) {
            throw new NullPointerException("Setter method cannot null");
        }
        if (setter.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Setter must have a single parameter");
        }
        
        // FIXME: should we check the setter's method annotations as well?
        this.qualifier = Qualifiers.getQualifier(setter.getParameterAnnotations()[0]);
        this.setter = setter;
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    public Method getSetterMethod() {
        return setter;
    }
    
    @Override
    public boolean isTransient() {
        // we'll check both setter and parameter annotations
        if (setter.getAnnotation(Transient.class) != null) {
            return true;
        }
        
        for (Annotation a: setter.getParameterAnnotations()[0]) {
            if (a instanceof Transient) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Class<?> getType() {
        return Types.box(setter.getParameterTypes()[0]);
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
        return ((SetterInjectionPoint) o).setter.equals(setter);
    }
    
    @Override
    public int hashCode() {
        return setter.hashCode();
    }
    
    @Override
    public String toString() {
        return "Setter(method=" + setter + ")";
    }
}
