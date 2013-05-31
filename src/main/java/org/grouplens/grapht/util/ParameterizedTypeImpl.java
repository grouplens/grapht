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
package org.grouplens.grapht.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

class ParameterizedTypeImpl implements ParameterizedType {
    private final Class<?> rawType;
    private final Type[] arguments;

    @SuppressWarnings("PMD.ArrayIsStoredDirectly")
    ParameterizedTypeImpl(Class<?> rawType, Type[] arguments) {
        this.rawType = rawType;
        this.arguments = arguments;
    }
    
    @Override
    public Type[] getActualTypeArguments() {
        // defensive copy
        return Arrays.copyOf(arguments, arguments.length);
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType t = (ParameterizedType) o;
        return t.getRawType().equals(rawType) && 
               t.getOwnerType() == null &&
               Arrays.equals(arguments, t.getActualTypeArguments());
    }
    
    @Override
    public int hashCode() {
        return rawType.hashCode() ^ Arrays.hashCode(arguments);
    }
}
