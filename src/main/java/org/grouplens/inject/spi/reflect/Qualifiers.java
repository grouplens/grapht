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
package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Named;

import org.grouplens.inject.spi.Qualifier;

/**
 * Utilities related to Qualifier implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class Qualifiers {
    private Qualifiers() { }
    
    /**
     * Return true if <tt>child</tt> is a sub-qualifier of <tt>parent</tt>. Either
     * Qualifier can be null to signify the default Qualifier. False is returned if the
     * child Qualifier does not inherit from the parent Qualifier.
     * 
     * @param child The potential child Qualifier
     * @param parent The parent Qualifier
     * @return True or false if child inherits from parent
     */
    public static boolean inheritsQualifier(@Nullable Qualifier child, @Nullable Qualifier parent) {
        return getQualifierDistance(child, parent) >= 0;
    }

    /**
     * Return the distance between the two Qualifiers. 0 is returned if the two Qualifiers
     * are equal. A negative number is returned if the child Qualifier does not
     * inherit the parent Qualifier.
     * 
     * @param child The potential child Qualifier, null for the default
     * @param parent The parent Qualifier, null for the default
     * @return The distance between the two Qualifiers
     */
    public static int getQualifierDistance(@Nullable Qualifier child, 
                                           @Nullable Qualifier parent) {
        if (child == null && parent == null) {
            // special case when both parent and child are the default qualifier
            return 0;
        }
        int distance = 0;
        
        if (parent != null) {
            // make sure the child qualifier inherits from the parent,
            // since parent is not null we don't care if the child inherits
            // from the default
            while(child != null) {
                if (child.equals(parent)) {
                    // the original child eventually inherits from the parent
                    return distance;
                }
                distance++;
                child = child.getParent();
            }
            
            // at this point the child cannot extend from the parent
            return -1;
        } else {
            // make sure the child qualifier inherits from the default
            while(child != null) {
                distance++;
                if (child.inheritsDefault()) {
                    // the child inherits the default
                    return distance;
                }
                child = child.getParent();
            }
            
            // at this point, none of the child's parents inherit from the default
            return -1;
        }
    }
    
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
