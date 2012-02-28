/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
import java.lang.reflect.Type;

import javax.inject.Provider;

import org.grouplens.inject.annotation.PassThrough;
import org.grouplens.inject.types.TypeAssignment;
import org.grouplens.inject.types.Types;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class SetterInjectionPoint implements InjectionPoint {
    private final Method setter;
    private final AnnotationRole role;
    private final boolean forProvider;
    
    private final TypeAssignment assignment;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     * @param assignment The assignment to apply to the injection point
     * @throws NullPointerException if setter or assignment are null
     * @throws IllegalArgumentException if the setter has more than one
     *             parameter
     */
    public SetterInjectionPoint(Method setter, TypeAssignment assignment) {
        if (setter == null || assignment == null) {
            throw new NullPointerException("Setter method and assignment cannot be null");
        }
        if (setter.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Setter must have a single parameter");
        }
        
        this.role = AnnotationRole.getRole(setter.getParameterAnnotations()[0]);
        this.setter = setter;
        this.assignment = assignment;
        this.forProvider = Provider.class.isAssignableFrom(setter.getDeclaringClass());
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    public Method getSetterMethod() {
        return setter;
    }
    
    @Override
    public boolean isTransient() {
        // the desire is transient if it's a dependency for a provider but 
        // is not a pass-through dependency (i.e. it's not used by the provided
        // object after creation).
        return forProvider && setter.getAnnotation(PassThrough.class) == null;
    }

    @Override
    public Type getType() {
        return assignment.apply(Types.box(setter.getGenericParameterTypes()[0]));
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }
    
    @Override
    public TypeAssignment getTypeAssignment() {
        return assignment;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SetterInjectionPoint)) {
            return false;
        }
        SetterInjectionPoint s = (SetterInjectionPoint) o;
        return s.setter.equals(setter) && s.assignment.equals(assignment);
    }
    
    @Override
    public int hashCode() {
        return setter.hashCode() ^ assignment.hashCode();
    }
    
    @Override
    public String toString() {
        return "Setter(method=" + setter + ")";
    }
}
