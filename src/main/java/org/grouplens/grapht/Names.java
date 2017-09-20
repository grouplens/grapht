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
package org.grouplens.grapht;

import org.grouplens.grapht.annotation.AnnotationBuilder;

import javax.inject.Named;


/**
 * Names is a utility class to create {@link Named} annotation instances when
 * configuring an injector that relies on named qualifiers. Qualifiers with
 * other attributes can by instantiated using the {@link AnnotationBuilder}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class Names {
    /**
     * Get a Named annotation instance whose value equals the provided String.
     * The returned annotation is equal to annotations created by a declaration
     * matching: <code>@Named(name)</code>, where name is the input String.
     * 
     * @param name The name value for the returned annotation
     * @return A Named instance wrapping the given name
     */
    public static Named named(String name) {
        return new AnnotationBuilder<Named>(Named.class)
            .setValue(name)
            .build();
    }
}
