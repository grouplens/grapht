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

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.types.TypeAssignment;
import org.grouplens.inject.types.Types;

import com.google.common.base.Function;

/**
 * ClassSatisfaction is a satisfaction that instantiates instances of a given
 * type.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ClassSatisfaction extends ReflectionSatisfaction {
    private final TypeAssignment assignment;
    private final Class<?> type;

    /**
     * Create a satisfaction wrapping the given class type. TypeAssignment is an
     * assignment of TypeVariables so that the class has no more free variables.
     * 
     * @param type The type to wrap
     * @param assignment The TypeAssignment to apply to the type
     * @throws NullPointerException if type or assignment is null
     * @throws IllegalArgumentException if the type cannot be instantiated
     */
    public ClassSatisfaction(Class<?> type, TypeAssignment assignment) {
        if (type == null) {
            throw new NullPointerException("Class type cannot be null");
        }
        if (assignment == null) {
            throw new NullPointerException("TypeAssignment cannot be null");
        }
        
        type = (Class<?>) Types.box(type);
        if (!Types.isInstantiable(type)) {
            throw new IllegalArgumentException("Type cannot be instantiated");
        }
        this.type = type;
        this.assignment = assignment;
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return ReflectionDesire.getDesires(type, assignment);
    }

    @Override
    public Type getType() {
        return assignment.apply(type);
    }

    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return new InjectionProviderImpl(type, getDependencies(), dependencies);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassSatisfaction)) {
            return false;
        }
        ClassSatisfaction s = (ClassSatisfaction) o;
        return s.type.equals(type) && s.assignment.equals(assignment);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ assignment.hashCode();
    }
    
    @Override
    public String toString() {
        return "ClassSatisfaction(" + getType() + ")";
    }
}
