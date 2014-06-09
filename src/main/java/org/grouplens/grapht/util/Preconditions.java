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

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.InvalidBindingException;
import org.grouplens.grapht.reflect.Qualifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

/**
 * Utility to organize checks for common assertions, and to throw
 * appropriately worded exceptions on failure.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class Preconditions {
    private Preconditions() { }

    @SuppressWarnings("unchecked")
    public static void isQualifier(Class<?> type) {
        if (!Annotation.class.isAssignableFrom(type)) {
            throw new InvalidBindingException(type, "Type is not an Annotation");
        }
        if (!Qualifiers.isQualifier((Class<? extends Annotation>) type)) {
            throw new InvalidBindingException(type, "Annotation is not annotated with @Qualifier");
        }
    }
    
    public static void isAssignable(Class<?> source, Class<?> impl) {
        if (!source.isAssignableFrom(impl)) {
            throw new InvalidBindingException(impl, "Type is not assignable to " + source);
        }
    }

    public static void notNull(String name, Object value) {
        if (value == null) {
            throw new NullPointerException(name + " cannot be null");
        }
    }
    
    public static void inRange(int value, int min, int max) {
        if (value < min || value >= max) {
            throw new IndexOutOfBoundsException(value + " must be in [" + min + ", " + max + ")");
        }
    }
}
