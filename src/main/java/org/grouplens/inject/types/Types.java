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
package org.grouplens.inject.types;

import javax.inject.Provider;
import java.lang.reflect.*;

/**
 * Static helper methods for working with types.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class Types {
    private Types() {}

    /**
     * Return the boxed version of the given type if the type is primitive.
     * Otherwise, if the type is not a primitive the original type is returned.
     * As an example, int.class is converted to Integer.class.
     * 
     * @param type The possibly unboxed type
     * @return The boxed type
     */
    public static Class<?> box(Class<?> type) {
        if (int.class.equals(type)) {
            return Integer.class;
        } else if (short.class.equals(type)) {
            return Short.class;
        } else if (byte.class.equals(type)) {
            return Byte.class;
        } else if (long.class.equals(type)) {
            return Long.class;
        } else if (boolean.class.equals(type)) {
            return Boolean.class;
        } else if (char.class.equals(type)) {
            return Character.class;
        } else if (float.class.equals(type)) {
            return Float.class;
        } else if (double.class.equals(type)) {
            return Double.class;
        } else {
            return type;
        }
    }

    /**
     * Compute the erasure of a type.
     * 
     * @param type The type to erase.
     * @return The class representing the erasure of the type.
     * @throws IllegalArgumentException if <var>type</var> is unerasable (e.g.
     *             it is a type variable or a wildcard).
     */
    public static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type raw = pt.getRawType();
            try {
                return (Class<?>) raw;
            } catch (ClassCastException e) {
                throw new RuntimeException("raw type not a Class", e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return the type distance between the child and parent types. If the child
     * does not extend from parent, then a negative value is returned.
     * Otherwise, the number of steps between child and parent is returned. As
     * an example, if child is an immediate subclass of parent, then 1 is
     * returned. If child and parent are equal than 0 is returned.
     * 
     * @param child The child type
     * @param parent The parent type
     * @return The type distance
     * @throws NullPointerException if child or parent are null
     */
    public static int getTypeDistance(Class<?> child, Class<?> parent) {
        if (!parent.isAssignableFrom(child)) {
            // if child does not extend from the parent, return -1
            return -1;
        }
        
        // at this point we can assume at some point a superclass of child
        // will equal parent
        int distance = 0;
        while(!child.equals(parent)) {
            distance++;
            child = child.getSuperclass();
        }
        return distance;
    }
    
    /**
     * Get the type that is provided by a given implementation of
     * {@link Provider}.
     * 
     * @param providerClass The provider's class
     * @return The provided class type
     * @throws IllegalArgumentException if the class doesn't actually implement
     *             Provider
     */
    public static Class<?> getProvidedType(Class<? extends Provider<?>> providerClass) {
        // FIXME: I don't know if this is capable of getting the generics
        // properly, but that's not my concern right now
        try {
            return Types.box(providerClass.getMethod("get").getReturnType());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement get()");
        }
    }
    
    /**
     * Return true if the type is not abstract and not an interface.
     * 
     * @param type A class type
     * @return True if the class type is instantiable
     */
    public static boolean isInstantiable(Class<?> type) {
        return !Modifier.isAbstract(type.getModifiers()) && !type.isInterface();
    }

    public static ParameterizedType parameterizedType(Class<?> cls, Type... args) {
        TypeVariable<?>[] vars = cls.getTypeParameters();
        if (args.length != vars.length) {
            throw new IllegalArgumentException("wrong number of arguments");
        }
        return new ParameterizedTypeImpl(cls, args, null);
    }

    public static WildcardType wildcardExtends(Type... upperBounds) {
        return wildcardType(upperBounds, null);
    }

    public static WildcardType wildcardSuper(Type... lowerBounds) {
        return wildcardType(null, lowerBounds);
    }

    public static WildcardType wildcardType(Type[] upper, Type[] lower) {
        if (upper == null || upper.length == 0) {
            upper = new Type[]{Object.class};
        }
        if (lower == null) {
            lower = new Type[0];
        }
        return new WildcardTypeImpl(upper, lower);
    }
    public static WildcardType wildcardType() {
        return wildcardType(null, null);
    }
}
