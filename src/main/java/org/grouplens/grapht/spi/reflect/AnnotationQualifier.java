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

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

/**
 * AnnotationQualifier is a Qualifier implementation that wraps annotations
 * that have been annotated with {@link Qualifier}
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class AnnotationQualifier implements org.grouplens.grapht.spi.Qualifier {
    private final Annotation annot;

    /**
     * Create an AnnotationQualifier that wraps the given {@link Qualifier} annotation.
     * 
     * @param annot The annotation to wrap
     * @throws NullPointerException if annot is null
     * @throws IllegalArgumentException if the annotation type is not a {@link Qualifier}
     *             annotation
     */
    public AnnotationQualifier(Annotation annot) {
        Checks.notNull("annot", annot);
        Checks.isQualifier(annot.annotationType());
        this.annot = annot;
    }
    
    /**
     * @return The annotation wrapped by this qualifier
     */
    public Annotation getAnnotation() {
        return annot;
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
        return annot.toString();
    }
}
