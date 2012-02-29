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

import com.google.common.base.Function;

import java.lang.reflect.*;

/**
 * Visit a type, invoking a method appropriate based on its subtype.
 *
 * @author Michael Ekstrand
 */
public abstract class TypeVisitor<T> implements Function<Type, T> {
    /**
     * Apply this visitor to a type, invoking the proper method based on the type's
     * actual class.
     * @param type The type to visit.
     * @return The return value of the visit method.
     */
    @Override
    public final T apply(Type type) {
        if (type instanceof Class) {
            return visitClass((Class<?>) type);
        } else if (type instanceof TypeVariable) {
            return visitTypeVariable((TypeVariable<?>) type);
        } else if (type instanceof WildcardType) {
            return visitWildcard((WildcardType) type);
        } else if (type instanceof ParameterizedType) {
            return visitParameterizedType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            return visitGenericArrayType((GenericArrayType) type);
        } else {
            throw new IllegalArgumentException("unknown type of type");
        }
    }

    /**
     * Default vist method, called by all type-specific visit methods in the base class.
     * The base implementation of this method returns <tt>null</tt>; override it to use
     * other default behavior (e.g. throwing {@link IllegalArgumentException}).
     * @param type The type being visited.
     * @return The default computation result.
     */
    public T visitDefault(@SuppressWarnings("UnusedParameters") Type type) {
        return null;
    }

    public T visitClass(Class<?> cls) {
        return visitDefault(cls);
    }

    public T visitTypeVariable(TypeVariable<?> var) {
        return visitDefault(var);
    }
    
    public T visitWildcard(WildcardType var) {
        return visitDefault(var);
    }
    
    public T visitParameterizedType(ParameterizedType type) {
        return visitDefault(type);
    }
    
    public T visitGenericArrayType(GenericArrayType type) {
        return visitDefault(type);
    }
}
