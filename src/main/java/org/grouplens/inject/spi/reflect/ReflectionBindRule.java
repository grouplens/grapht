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

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;

abstract class ReflectionBindRule implements BindRule {
    private final AnnotationRole role;
    private final Class<?> sourceType;
    
    private final boolean generated;
    
    public ReflectionBindRule(AnnotationRole role, Class<?> sourceType, boolean generated) {
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
