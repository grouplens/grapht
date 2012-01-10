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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Class for expressing type literals. Create a concrete class extending
 * this class, instantiating <var>E</var> with a particular type, to express
 * that type statically.  This is pretty much like Guice's type literals.
 * <p/>
 * Example:
 * <pre>
 * {@code
 * TypeLiteral<List<String>> typ = new TypeLiteral<List<String>>() { }
 * }
 * </pre>
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <E> The type this literal is to express.
 */
public abstract class TypeLiteral<E> {
    private Type type;
    
    protected TypeLiteral() {
        type = extractType();
    }
    
    private Type extractType() {
        Type sc = getClass().getGenericSuperclass();
        try {
            ParameterizedType psc = (ParameterizedType) sc;
            if (!psc.getRawType().equals(TypeLiteral.class)) {
                throw new RuntimeException("TypeLiteral must be directly subclassed");
            }
            assert psc.getRawType().equals(TypeLiteral.class);
            return psc.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid subclassing", e);
        }
    }
    
	public Type getType() {
		return type;
	}
}
