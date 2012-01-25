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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import javax.inject.Provider;

import org.grouplens.inject.annotation.PassThrough;
import org.grouplens.inject.types.Types;

/**
 * ConstructorParameterInjectionPoint is an injection point wrapping a parameter
 * of a constructor.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ConstructorParameterInjectionPoint implements InjectionPoint {
    private final AnnotationRole role;
    private final Constructor<?> ctor;
    private final int parameter;
    private final boolean forProvider;

    /**
     * Create a ConstructorParameterInjectionPoint that wraps the given
     * parameter index for the given constructor, ctor.
     * 
     * @param ctor The constructor to wrap
     * @param parameter The parameter index of this injection point within
     *            ctor's parameters
     * @throws NullPointerException if ctor is null
     * @throws IndexOutOfBoundsException if parameter is not a valid index into
     *             the constructor's parameters
     */
    public ConstructorParameterInjectionPoint(Constructor<?> ctor, int parameter) {
        if (ctor == null) {
            throw new NullPointerException("Constructor cannot be null");
        }
        
        int numArgs = ctor.getParameterTypes().length;
        if (parameter < 0 || parameter >= numArgs) {
            throw new IndexOutOfBoundsException("Constructor parameter is invalid");
        }
        
        this.role = AnnotationRole.getRole(ctor.getParameterAnnotations()[parameter]);
        this.ctor = ctor;
        this.parameter = parameter;
        this.forProvider = Provider.class.isAssignableFrom(ctor.getDeclaringClass());
    }

    /**
     * @return The constructor wrapped by this injection point
     */
    public Constructor<?> getConstructor() {
        return ctor;
    }
    
    /**
     * @return The parameter index of this injection point within the
     *         constructor's parameters
     */
    public int getConstructorParameter() {
        return parameter;
    }
    
    @Override
    public boolean isTransient() {
        boolean passThrough = false;
        Annotation[] annots = ctor.getParameterAnnotations()[parameter];
        for (int i = 0; i < annots.length; i++) {
            if (annots[i] instanceof PassThrough) {
                passThrough = true;
                break;
            }
        }
        
        // the desire is transient if it is for a provider and the parameter has
        // not been annotated as a pass-through dependency
        return forProvider && !passThrough;
    }

    @Override
    public Class<?> getType() {
        return Types.box(ctor.getParameterTypes()[parameter]);
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorParameterInjectionPoint)) {
            return false;
        }
        ConstructorParameterInjectionPoint cp = (ConstructorParameterInjectionPoint) o;
        return cp.ctor.equals(ctor) && cp.parameter == parameter;
    }
    
    @Override
    public int hashCode() {
        return ctor.hashCode() ^ parameter;
    }
    
    @Override
    public String toString() {
        return "ConstructorParameter(ctor=" + ctor + ", param=" + parameter + ")";
    }
}
