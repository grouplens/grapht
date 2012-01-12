/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import org.grouplens.inject.annotation.DefaultBoolean;
import org.grouplens.inject.annotation.DefaultDouble;
import org.grouplens.inject.annotation.DefaultInt;
import org.grouplens.inject.annotation.DefaultString;
import org.grouplens.inject.annotation.DefaultType;
import org.grouplens.inject.annotation.InheritsDefaultRole;
import org.grouplens.inject.annotation.InheritsRole;
import org.grouplens.inject.annotation.Parameter;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Role;

/**
 * AnnotationRole is a Role implementation that wraps Roles and Parameters
 * described by the annotations defined in org.grouplens.inject.annotation.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class AnnotationRole implements Role {
    private final Class<? extends Annotation> roleType;

    /**
     * Create an AnnotationRole that wraps the given role annotation type.
     * 
     * @param roleType The annotation role type
     * @throws NullPointerException if roleType is null
     * @throws IllegalArgumentException if the annotation type is not a role
     *             annotation
     */
    public AnnotationRole(Class<? extends Annotation> roleType) {
        if (roleType == null)
            throw new NullPointerException("Role type cannot be null");
        if (!isRole(roleType))
            throw new IllegalArgumentException("Annotation is not a Role or Parameter annotation");
        this.roleType = roleType;
    }

    /**
     * Return true if <tt>child</tt> is a sub-role of <tt>parent</tt>. Either
     * role can be null to signify the default role. False is returned if the
     * child role does not inherit from the parent role.
     * 
     * @param child The potential child role
     * @param parent The parent role
     * @return True or false if child inherits from parent
     */
    public static boolean inheritsRole(AnnotationRole child, AnnotationRole parent) {
        if (parent != null) {
            // make sure the child role inherits from the parent
            while(child != null) {
                if (child.equals(parent)) {
                    // the original child eventually inherits from the parent
                    return true;
                }
                child = (child.inheritsRole() ? child.getParentRole() : null);
            }
            
            // at this point the child cannot extend from the parent
            return false;
        } else {
            // make sure the child role inherits from the default
            while(child != null) {
                if (!child.inheritsRole()) {
                    // the role does not inherit the default role
                    return false;
                }
                child = child.getParentRole();
            }
            
            // at this point, the child role inherits the default
            return true;
        }
    }
    
    /**
     * Return the AnnotationRole representing the role contained in the
     * parameter annotations given. If the parameter annotations do not have any
     * annotation that is a role or parameter, then null is returned.
     * 
     * @param parameterAnnots The parameter annotations on the setter or
     *            constructor
     * @return The AnnotationRole for the injection point, or null if there is
     *         no role
     */
    public static AnnotationRole getRole(Annotation[] parameterAnnots) {
        for (int i = 0; i < parameterAnnots.length; i++) {
            if (AnnotationRole.isRole(parameterAnnots[i].annotationType())) {
                return new AnnotationRole(parameterAnnots[i].annotationType());
            }
        }
        return null;
    }

    /**
     * Return true or false whether or not the annotation type represents a
     * {@link org.grouplens.inject.annotation.Role} or {@link Parameter}
     * 
     * @param type The annotation type
     * @return True if the annotation is a role or parameter
     * @throws NullPointerException if the type is null
     */
    public static boolean isRole(Class<? extends Annotation> type) {
        return (type.getAnnotation(org.grouplens.inject.annotation.Role.class) != null ||
                type.getAnnotation(org.grouplens.inject.annotation.Parameter.class) != null);
    }
    
    /**
     * @return True if the annotation is a Parameter versus a Role
     */
    public boolean isParameter() {
        return roleType.getAnnotation(Parameter.class) != null;
    }
    
    /**
     * @return The annotation type wrapped by this role
     */
    public Class<? extends Annotation> getRoleType() {
        return roleType;
    }

    /**
     * Return true if this role inherits from a parent role. If this returns
     * true, and {@link #getParentRole()} returns null it means that it inherits
     * from the default role.
     * 
     * @return True if the role inherits
     */
    public boolean inheritsRole() {
        return roleType.getAnnotation(InheritsRole.class) != null || roleType.getAnnotation(InheritsDefaultRole.class) != null;
    }

    /**
     * Return the parent role of this annotation. This will return null if the
     * role does not inherit, or if it inherits from the default role. These can
     * be distinguished by checking {@link #inheritsRole()}. If a non-null role
     * is returned, then inheritsRole() will return true.
     * 
     * @return The parent role this role inherits from
     */
    public AnnotationRole getParentRole() {
        InheritsRole parentRole = roleType.getAnnotation(InheritsRole.class);
        if (parentRole != null) {
            return new AnnotationRole(parentRole.value());
        }
        
        // The parent role is still null if InheritsDefaultRole is on the
        // annotation, inheritsRole() distinguishes the cases.
        return null;
    }

    /**
     * @return A default bind rule declared for this role, or null if the
     *         annotation role has no binding
     */
    public BindRule getDefaultBinding() {
        if (isParameter()) {
            DefaultDouble dfltDouble = roleType.getAnnotation(DefaultDouble.class);
            if (dfltDouble != null) {
                // FIXME: return a parameter binding/instance binding
            }
            DefaultInt dfltInt = roleType.getAnnotation(DefaultInt.class);
            if (dfltInt != null) {
                // FIXME: return a ""
            }
            DefaultBoolean dfltBool = roleType.getAnnotation(DefaultBoolean.class);
            if (dfltBool != null) {
                // FIXME: return a ""
            }
            DefaultString dfltStr = roleType.getAnnotation(DefaultString.class);
            if (dfltStr != null) {
                // FIXME: return a ""
            }
        } else {
            DefaultType impl = roleType.getAnnotation(DefaultType.class);
            if (impl != null) {
                // FIXME: return a type bind rule
            }
        }
        
        // if there is no specified default, return a null binding
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationRole)) {
            return false;
        }
        return ((AnnotationRole) o).roleType.equals(roleType);
    }
    
    @Override
    public int hashCode() {
        return roleType.hashCode();
    }
}
