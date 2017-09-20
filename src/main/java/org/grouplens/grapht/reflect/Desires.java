/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.reflect.internal.ReflectionDesire;
import org.grouplens.grapht.reflect.internal.SimpleInjectionPoint;

import org.jetbrains.annotations.Nullable;
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
