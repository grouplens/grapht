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

import javax.inject.Qualifier;

import org.grouplens.inject.annotation.InheritsDefaultQualifier;
import org.grouplens.inject.annotation.InheritsQualifier;

/**
 * AnnotationQualifier is a Qualifier implementation that wraps annotations
 * that have been annotated with {@link Qualifier}
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
        if (!Qualifiers.isQualifier(qualifierType))
            throw new IllegalArgumentException("Annotation is not a Qualifier annotation");
        annot = qualifierType;
    }
    
    /**
     * @return The annotation type wrapped by this qualifier
     */
    public Class<? extends Annotation> getAnnotation() {
        return annot;
    }

    @Override
    public boolean inheritsDefault() {
        // we add getParent() == null because this should only return true
        // if there is no other parent
        return annot.getAnnotation(InheritsDefaultQualifier.class) != null && getParent() == null;
    }

    @Override
    public AnnotationQualifier getParent() {
        InheritsQualifier parentRole = annot.getAnnotation(InheritsQualifier.class);
        if (parentRole != null) {
            return new AnnotationQualifier(parentRole.value());
        }
        
        // The parent qualifier is still null if InheritsDefaultQualifier 
        // is applied to this annotaiton
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
        return "@" + annot.getSimpleName();
    }
    
    /*
     * AnnotationQualifier delegates to the Annotation class type for its
     * implementation of AnnotatedElement.
     */

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annot.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return annot.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annot.getDeclaredAnnotations();
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return annot.isAnnotationPresent(annotationClass);
    }
}
