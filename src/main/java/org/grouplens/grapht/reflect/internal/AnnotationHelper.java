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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.annotation.Attribute;
import org.grouplens.grapht.reflect.Qualifiers;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for managing annotations on an injection point.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
class AnnotationHelper {
    private final Map<Class<? extends Annotation>, Annotation> attrs;
    private final Annotation qualifier;
    
    public AnnotationHelper(Annotation... annots) {
        attrs = new HashMap<Class<? extends Annotation>, Annotation>();
        Annotation foundQualifier = null;
        for (Annotation a: annots) {
            if (a.annotationType().getAnnotation(Attribute.class) != null) {
                // a is an attribute
                attrs.put(a.annotationType(), a);
            }
            if (foundQualifier == null && Qualifiers.isQualifier(a.annotationType())) {
                // a is a qualifier
                foundQualifier = a;
            }
        }
        
        qualifier = foundQualifier;
    }
    
    public Annotation getQualifier() {
        return qualifier;
    }

    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return atype.cast(attrs.get(atype));
    }
    
    public Collection<Annotation> getAttributes() {
        return Collections.unmodifiableCollection(attrs.values());
    }
}
