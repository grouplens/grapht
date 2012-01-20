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

import javax.annotation.Nullable;

import org.grouplens.inject.spi.Desire;

/**
 * ClassBindRule is a reflection bind rule that binds a subclass to a
 * higher-level type. If the implementation type is instantiable, the bind rule
 * will satisfy all matching desires with a {@link ClassSatisfaction}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ClassBindRule extends ReflectionBindRule {
    private final Class<?> implType;

    /**
     * Create a ClassBindRule that binds <tt>implType</tt> to any desire for
     * <tt>sourceType</tt>.
     * 
     * @param implType The implementation type that satisfies the desire
     * @param sourceType The source type matched by this bind rule
     * @param role The role matched by this desire
     * @param weight The weight of the bind rule
     * @throws NullPointerException if implType or sourceType are null
     * @throws IllegalArgumentException if implType does not extend sourceType
     */
    public ClassBindRule(Class<?> implType, Class<?> sourceType, @Nullable AnnotationRole role,  int weight) {
        super(sourceType, role, weight);
        if (implType == null) {
            throw new NullPointerException("Implementation type cannot be null");
        }
        
        implType = Types.box(implType);
        if (!Types.box(sourceType).isAssignableFrom(implType)) {
            throw new IllegalArgumentException(implType + " does not extend " + sourceType);
        }
        this.implType = implType;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire rd = (ReflectionDesire) desire;
        // we can pass in null for the satisfaction here, because ReflectionDesire
        // will create a ClassSatisfaction for us if implType is instantiable
        return new ReflectionDesire(implType, rd.getInjectionPoint(), null);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassBindRule)) {
            return false;
        }
        return super.equals(o) && ((ClassBindRule) o).implType.equals(implType);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ implType.hashCode();
    }
    
    @Override
    public String toString() {
        return "ClassBindRule(" + getRole() + ":" + getSourceType() + " -> " + implType + ")";
    }
}
