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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.annotation.Attribute;
import org.grouplens.grapht.spi.Attributes;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Basic implementation of {@link Attributes} based on the {@link Annotation}
 * array reported by the injection point.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class AttributesImpl implements Attributes {
    private final Map<Class<? extends Annotation>, Annotation> attrs;
    private final Annotation qualifier;
    
    public AttributesImpl(Annotation... annots) {
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
    
    @Override
    public Annotation getQualifier() {
        return qualifier;
    }

    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return atype.cast(attrs.get(atype));
    }
    
    @Override
    public Collection<Annotation> getAttributes() {
        return attrs.values();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttributesImpl)) {
            return false;
        }
        
        AttributesImpl a = (AttributesImpl) o;
        return (qualifier == null ? a.qualifier == null : qualifier.equals(a.qualifier)) && attrs.equals(a.attrs);
    }
    
    @Override
    public int hashCode() {
        return (qualifier == null ? 0 : qualifier.hashCode()) ^ attrs.hashCode();
    }
}
