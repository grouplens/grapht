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
