package org.grouplens.grapht.spi;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Qualifier;

/**
 * Attributes contain additional annotations and metadata associated with an
 * injection point. This includes any {@link Qualifier} annotation that is
 * applied. Additional attributes can be defined by creating a new annotation
 * type that is itself annotated with {@link Attribute}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Attributes {
    /**
     * Return the qualifier annotation added to the injection point. The
     * returned annotation's type will have been annotated with
     * {@link Qualifier}. If the injection point is not qualified, this will
     * return null.
     * 
     * @return Any qualifier applied to the injection point
     */
    @Nullable Annotation getQualifier();
    
    /**
     * Return the attribute of type A that is applied to the injection point. If
     * the injection point does not have an attribute of A, then null is
     * returned.
     * 
     * @param atype Attribute annotation type
     * @return The instance of A applied to the injection point, or null
     * @throws NullPointerException if atype is null
     */
    @Nullable <A extends Annotation> A getAttribute(Class<A> atype);
}
