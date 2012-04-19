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

import javax.inject.Named;

import org.grouplens.grapht.spi.Qualifier;

/**
 * Utilities related to Qualifier implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class Qualifiers {
    private Qualifiers() { }
    
    /**
     * Return the Qualifier representing the {@link Qualifier} contained in the
     * parameter annotations given. If the annotations do not have any
     * annotation that is a {@link Qualifier}, then null is returned. If
     * {@link Named} is encountered, a NamedQualifier is used, otherwise a
     * {@link AnnotationQualifier} is used.
     * 
     * @param parameterAnnots The parameter annotations on the setter or
     *            constructor
     * @return The Qualifier for the injection point, or null if there
     *         is no {@link Qualifier}
     */
    public static Qualifier getQualifier(Annotation[] parameterAnnots) {
        for (int i = 0; i < parameterAnnots.length; i++) {
            if (Qualifiers.isQualifier(parameterAnnots[i].annotationType())) {
                if (parameterAnnots[i] instanceof Named) {
                    // special case to extract the annotated name
                    return new NamedQualifier(((Named) parameterAnnots[i]).value());
                } else {
                    // wrap all other qualifier annotations with AnnotationQualifier
                    return new AnnotationQualifier(parameterAnnots[i].annotationType());
                }
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
        return type.getAnnotation(javax.inject.Qualifier.class) != null;
    }
}
