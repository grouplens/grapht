/*
 * LensKit, a reference implementation of recommender algorithms.
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

import org.grouplens.inject.spi.Satisfaction;

// FIXME: repair documentation to refer to satisfactions instead of nodes
public class ClasspathSatisfactionRepository {

    /**
     * Create a new node wrapping an instance. The object must be of a
     * non-parameterized type.
     * @param obj The object to wrap.
     * @return A node which, when instantiated, returns <var>obj</var>.
     * @throws IllegalArgumentException if <var>obj</var> is an instance of a
     * parameterized type.
     */
    public Satisfaction newInstanceNode(Object obj) {
        Class<?> type = obj.getClass();
        if (type.getTypeParameters().length > 0) {
            throw new IllegalArgumentException("object is of parameterized type");
        }
        return newInstanceNode(obj, type);
    }

    /**
     * Create a new node wrapping an instance.
     * @param obj The object instance to wrap.
     * @param type The type of the object.
     * @return A node which, when instantiated, returns <var>obj</var>.
     * @throws IllegalArgumentException if <var>obj</var> is not, as far as the
     * code can tell, of type <var>type</var>. Due to type reification, not all
     * such errors can be caught.
     */
    public Satisfaction newInstanceNode(Object obj, Type type) {
        return new InstanceSatisfaction(obj, type);
    }

    /**
     * Create a new node wrapping an instance.
     * @param obj The object.
     * @param type The type of the object.
     * @param <T> The type of the object.
     * @return A node wrapping the object.
     * @see #newInstanceNode(Object, java.lang.reflect.Type) 
     */
    public <T> Satisfaction newInstanceNode(T obj, TypeLiteral<T> type) {
        return newInstanceNode(obj, type.getType());
    }
}
