/*
 * LensKit, an open source recommender systems toolkit.
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

import java.lang.reflect.Type;

import javax.annotation.Nullable;

import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.SatisfactionAndRole;
import org.grouplens.inject.types.Types;

/**
 * ReflectionContextMatcher is a ContextMatcher that matches nodes if the node's
 * type inherits from the matcher's type and if the node's role inherits from
 * the matcher's role.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionContextMatcher implements ContextMatcher {
    private final Type type;
    private final AnnotationRole role;

    /**
     * Create a ReflectionContextMatcher that matches the given type and the
     * default role.
     * 
     * @param type The type to match
     */
    public ReflectionContextMatcher(Type type) {
        this(type, null);
    }

    /**
     * Create a ReflectionContextMatcher that matches the given type and the
     * given role.
     * 
     * @param type The type to match
     * @param role The role to match
     */
    public ReflectionContextMatcher(Type type, @Nullable AnnotationRole role) {
        this.type = type;
        this.role = role;
    }
    
    /**
     * @return The type matched by this matcher
     */
    public Type getMatchedType() {
        return type;
    }
    
    /**
     * @return The role matched by this matcher
     */
    public AnnotationRole getMatchedRole() {
        return role;
    }
    
    @Override
    public boolean matches(SatisfactionAndRole n) {
        // context matching follows Java's type inheritence,
        //  i.e. the type of the satisfaction must be a subclass
        //   and the type variables must fit the bounds
        if (Types.erase(type).isAssignableFrom(n.getSatisfaction().getErasedType())) {
            // type is a match, so check type variables
            
            
            
            return AnnotationRole.inheritsRole((AnnotationRole) n.getRole(), role);
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionContextMatcher)) {
            return false;
        }
        ReflectionContextMatcher r = (ReflectionContextMatcher) o;
        return r.type.equals(type) && (r.role == null ? role == null : r.role.equals(role));
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (role == null ? 0 : role.hashCode());
    }
    
    @Override
    public String toString() {
        return "ReflectionContextMatcher(" + role + ":" + type + ")";
    }
}
