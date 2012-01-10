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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

/**
 * Static helper methods for working with types.
 */
public final class Types {
    private Types() {}

    /**
     * Compute the erasure of a type.
     * @param type The type to erase.
     * @return The class representing the erasure of the type.
     * @throws IllegalArgumentException if <var>type</var> is unerasable (e.g. it is a type
     * variable or a wildcard).
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
            return providerClass.getMethod("get").getReturnType();
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement get()");
        }
    }
    
    public static boolean isInstantiable(Class<?> type) {
        return !Modifier.isAbstract(type.getModifiers()) && !type.isInterface();
    }
    
    public static List<Desire> getProviderDesires(Class<? extends Provider<?>> providerType) {
        return getDesires(providerType, true);
    }
    
    public static List<Desire> getDesires(Class<?> type) {
        return getDesires(type, false);
    }
    
    private static List<Desire> getDesires(Class<?> type, boolean isProvider) {
        List<Desire> desires = new ArrayList<Desire>();

        boolean ctorFound = false;
        for (Constructor<?> ctor: type.getConstructors()) {
            if (ctor.getAnnotation(Inject.class) != null) {
                if (!ctorFound) {
                    ctorFound = true;
                    for (int i = 0; i < ctor.getParameterTypes().length; i++) {
                        desires.add(new ConstructorParameterDesire(ctor, i, isProvider));
                    }
                } else {
                    // at the moment there can only be one injectable constructor
                    // FIXME: return a better exception with more information
                    throw new RuntimeException("Too many injectable constructors");
                }
            }
        }
        
        for (Method m: type.getMethods()) {
            if (m.getAnnotation(Inject.class) != null) {
                if (m.getParameterTypes().length != 1 || m.getReturnType() != null) {
                    // invalid setter injection point
                    // FIXME: better exception as above
                    throw new RuntimeException("Invalid setter injection method");
                }
                
                desires.add(new SetterMethodDesire(m, isProvider));
            }
        }
        
        return Collections.unmodifiableList(desires);
    }
}
