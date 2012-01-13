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

import javax.annotation.Nullable;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;

/**
 * ReflectionBindRule is an abstract implementation of BindRule. It is a partial
 * function from desires to desires. Its matching logic only depends on the
 * source type and role of the rule, and not what the function produces. A
 * ReflectionBindRule will only match a desire if the desire's desired type
 * equals the source type, and only if the desire's role inherits from the role
 * of the bind rule.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public abstract class ReflectionBindRule implements BindRule {
    private final AnnotationRole role;
    private final Class<?> sourceType;
    
    private final boolean generated;

    /**
     * Create a bind rule that matches a desire when the desired type equals
     * <tt>sourceType</tt> and the desire's role inherits from <tt>role</tt>.
     * <tt>generated</tt> is a flag of whether or not this bind rule was
     * automatically generated for a user or if it was declared manually.
     * 
     * @param sourceType The source type this bind rule matches
     * @param role The role the bind rule applies to
     * @param generated True if the bind rule was automatically generated
     * @throws NullPointerException if sourceType is null
     */
    public ReflectionBindRule(Class<?> sourceType, @Nullable AnnotationRole role, boolean generated) {
        if (sourceType == null) {
            throw new NullPointerException("Source type cannot be null");
        }
        
        this.role = role;
        this.sourceType = sourceType;
        this.generated = generated;
    }

    /**
     * @return True if this was a generated binding, false if the binding was
     *         specified manually by a programmer or config file
     */
    public boolean isGenerated() {
        return generated;
    }
    
    /**
     * @return The annotation role matched by this bind rule
     */
    public AnnotationRole getRole() {
        return role;
    }
    
    /**
     * @return The source type matched by this bind rule
     */
    public Class<?> getSourceType() {
        return sourceType;
    }
    
    @Override
    public boolean matches(Desire desire) {
        ReflectionDesire rd = (ReflectionDesire) desire;
        // bind rules match type by equality
        if (rd.getDesiredType().equals(sourceType)) {
            // if the type is equal, then the roles match if
            // the desires role is a subtype of the bind rules role
            return AnnotationRole.inheritsRole(rd.getRole(), role);
        }
        
        // the type and roles are not a match, so return false
        return false;
    }
}
