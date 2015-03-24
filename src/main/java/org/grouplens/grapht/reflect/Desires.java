/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.reflect.internal.ReflectionDesire;
import org.grouplens.grapht.reflect.internal.SimpleInjectionPoint;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Desires {
    private Desires() {}

    /**
     * Create a new desire.
     * @param qualifier The qualifier applied to the type.
     * @param type The desired type.
     * @param nullable Whether this injection is nullable.
     * @return The desire.
     */
    public static Desire create(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        return new ReflectionDesire(createInjectionPoint(qualifier, type, nullable));
    }

    public static InjectionPoint createInjectionPoint(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        return new SimpleInjectionPoint(qualifier, type, nullable);
    }
}
