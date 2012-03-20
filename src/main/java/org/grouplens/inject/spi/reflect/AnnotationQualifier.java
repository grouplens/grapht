/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Qualifier;

import org.grouplens.inject.annotation.InheritsDefaultQualifier;
import org.grouplens.inject.annotation.InheritsQualifier;

/**
 * AnnotationQualifier is a Qualifier implementation that wraps the
 * {@link Qualifier} annotation.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class AnnotationQualifier implements org.grouplens.inject.spi.Qualifier {
    private final Class<? extends Annotation> annot;

    /**
     * Create an AnnotationQualifier that wraps the given {@link Qualifier} annotation type.
     * 
     * @param qualifierType The annotation {@link Qualifier} type
     * @throws NullPointerException if {@link Qualifier}Type is null
     * @throws IllegalArgumentException if the annotation type is not a {@link Qualifier}
     *             annotation
     */
    public AnnotationQualifier(Class<? extends Annotation> qualifierType) {
        if (qualifierType == null)
            throw new NullPointerException("Qualifier type cannot be null");
        if (!isQualifier(qualifierType))
            throw new IllegalArgumentException("Annotation is not a Qualifier annotation");
        annot = qualifierType;
    }

    /**
     * Return true if <tt>child</tt> is a sub-qualifier of <tt>parent</tt>. Either
     * Qualifier can be null to signify the default Qualifier. False is returned if the
     * child Qualifier does not inherit from the parent Qualifier.
     * 
     * @param child The potential child Qualifier
     * @param parent The parent Qualifier
     * @return True or false if child inherits from parent
     */
    public static boolean inheritsQualifier(AnnotationQualifier child, AnnotationQualifier parent) {
        return getQualifierDistance(child, parent) >= 0;
    }

    /**
     * Return the distance between the two Qualifiers. 0 is returned if the two Qualifiers
     * are equal. A negative number is returned if the child Qualifier does not
     * inherit the parent Qualifier.
     * 
     * @param child The potential child Qualifier
     * @param parent The parent Qualifier
     * @return The distance between the two Qualifiers
     */
    public static int getQualifierDistance(@Nullable AnnotationQualifier child, 
                                           @Nullable AnnotationQualifier parent) {
        int distance = 0;
        
        if (parent != null) {
            // make sure the child {@link Qualifier} inherits from the parent
            while(child != null) {
                if (child.equals(parent)) {
                    // the original child eventually inherits from the parent
                    return distance;
                }
                distance++;
                child = (child.inheritsQualifier() ? child.getParentQualifier() : null);
            }
            
            // at this point the child cannot extend from the parent
            return -1;
        } else {
            // make sure the child {@link Qualifier} inherits from the default
            while(child != null) {
                if (!child.inheritsQualifier()) {
                    // the {@link Qualifier} does not inherit the default {@link Qualifier}
                    return -1;
                }
                distance++;
                child = child.getParentQualifier();
            }
            
            // at this point, the child {@link Qualifier} inherits the default
            return distance;
        }
    }
    
    /**
     * Return the AnnotationQualifier representing the {@link Qualifier} contained in the
     * parameter annotations given. If the parameter annotations do not have any
     * annotation that is a {@link Qualifier} or parameter, then null is returned.
     * 
     * @param parameterAnnots The parameter annotations on the setter or
     *            constructor
     * @return The AnnotationQualifier for the injection point, or null if there is
     *         no {@link Qualifier}
     */
    public static AnnotationQualifier getQualifier(Annotation[] parameterAnnots) {
        for (int i = 0; i < parameterAnnots.length; i++) {
            if (AnnotationQualifier.isQualifier(parameterAnnots[i].annotationType())) {
                return new AnnotationQualifier(parameterAnnots[i].annotationType());
            }
        }
        return null;
    }

    /**
     * Return true or false whether or not the annotation type represents a
     * {@link Qualifier}
     * 
     * @param type The annotation type
     * @return True if the annotation is a {@link Qualifier} or parameter
     * @throws NullPointerException if the type is null
     */
    public static boolean isQualifier(Class<? extends Annotation> type) {
        return type.getAnnotation(Qualifier.class) != null;
    }
    
    /**
     * @return The annotation type wrapped by this {@link Qualifier}
     */
    public Class<? extends Annotation> getQualifierAnnotation() {
        return annot;
    }

    /**
     * Return true if this {@link Qualifier} inherits from a parent {@link Qualifier}. If this returns
     * true, and {@link #getParentRole()} returns null it means that it inherits
     * from the default {@link Qualifier}.
     * 
     * @return True if the {@link Qualifier} inherits
     */
    public boolean inheritsQualifier() {
        return annot.getAnnotation(InheritsQualifier.class) != null || annot.getAnnotation(InheritsDefaultQualifier.class) != null;
    }

    /**
     * Return the parent Qualifier of this qualifier. This will return null if
     * the Qualifier does not inherit, or if it inherits from the default
     * Qualifier. These can be distinguished by checking
     * {@link #inheritsQualifier()}. If a non-null Qualifier is returned, then
     * inheritsQualifier() will return true.
     * 
     * @return The parent Qualifier this Qualifier inherits from
     */
    public AnnotationQualifier getParentQualifier() {
        InheritsQualifier parentRole = annot.getAnnotation(InheritsQualifier.class);
        if (parentRole != null) {
            return new AnnotationQualifier(parentRole.value());
        }
        
        // The parent {@link Qualifier} is still null if InheritsDefaultQualifier is on the
        // annotation, inheritsQualifier() distinguishes the cases.
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationQualifier)) {
            return false;
        }
        return ((AnnotationQualifier) o).annot.equals(annot);
    }
    
    @Override
    public int hashCode() {
        return annot.hashCode();
    }
    
    @Override
    public String toString() {
        return "AnnotationQualifier(" + annot + ")";
    }
}
