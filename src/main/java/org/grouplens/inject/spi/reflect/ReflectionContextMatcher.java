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

import org.grouplens.inject.resolver.ContextMatcher;
import org.grouplens.inject.spi.SatisfactionAndRole;

class ReflectionContextMatcher implements ContextMatcher {
    // FIXME: will this have to be changed to Type?
    private final Class<?> type;
    private final AnnotationRole role;
    
    public ReflectionContextMatcher(Class<?> type) {
        this(type, null);
    }
    
    public ReflectionContextMatcher(Class<?> type, AnnotationRole role) {
        this.type = type;
        this.role = role;
    }
    
    @Override
    public boolean matches(SatisfactionAndRole n) {
        // FIXME: handle generics correctly
        if (type.isAssignableFrom(n.getSatisfaction().getErasedType())) {
            // type is a match, so check the role
            AnnotationRole current = (AnnotationRole) n.getRole();
            if (role != null) {
                // make sure the satisfaction's role inherits from this role
                while(current != null) {
                    if (current.equals(role)) {
                        // the satisfaction's role inherits from the matcher's role
                        return true;
                    }
                    current = (current.inheritsRole() ? current.getParentRole() : null);
                }
                
                // at this point the role does not extend from the matcher's role
                return false;
            } else {
                // make sure the satisfaction's role inherits from the default
                while(current != null) {
                    if (!current.inheritsRole()) {
                        // the role does not inherit the default role
                        return false;
                    }
                    current = current.getParentRole();
                }
                
                // at this point, the role inherits the default
                return true;
            }
        }
        
        return false;
    }
}
