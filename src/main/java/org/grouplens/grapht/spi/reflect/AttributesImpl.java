package org.grouplens.grapht.spi.reflect;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import org.grouplens.grapht.annotation.Attribute;
import org.grouplens.grapht.spi.Attributes;

/**
 * Basic implementation of {@link Attributes} based on the {@link Annotation}
 * array reported by the injection point.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class AttributesImpl implements Attributes {
    private final Map<Class<? extends Annotation>, Annotation> attrs;
    private final Annotation qualifier;
    
    public AttributesImpl(Annotation[] annots) {
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
}
