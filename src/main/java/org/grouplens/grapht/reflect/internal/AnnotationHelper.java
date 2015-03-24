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
