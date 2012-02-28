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

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.reflect.ReflectionDesire.DefaultSource;
import org.grouplens.inject.types.TypeAssignment;
import org.grouplens.inject.types.Types;

/**
 * TypeBindRule is a reflection bind rule that binds a subclass to a
 * higher-level type. If the implementation type is instantiable, the bind rule
 * will satisfy all matching desires with a {@link ClassSatisfaction}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class TypeBindRule extends ReflectionBindRule {
    private final Class<?> implType;

    /**
     * Create a TypeBindRule that binds <tt>implType</tt> to any desire for
     * <tt>sourceType</tt>.
     * 
     * @param implType The implementation type that satisfies the desire
     * @param sourceType The source type matched by this bind rule
     * @param role The role matched by this desire
     * @param weight The weight of the bind rule
     * @throws NullPointerException if implType or sourceType are null
     * @throws IllegalArgumentException if implType does not extend sourceType
     */
    public TypeBindRule(Class<?> implType, Type sourceType, @Nullable AnnotationRole role,  int weight) {
        super(sourceType, role, weight);
        if (implType == null) {
            throw new NullPointerException("Implementation type cannot be null");
        }
        
        implType = (Class<?>) Types.box(implType);
        if (Types.findCompatibleAssignment(implType, sourceType) == null) {
            throw new IllegalArgumentException(implType + " does not extend " + sourceType);
        }
        this.implType = implType;
    }
    
    @Override
    public boolean terminatesChain() {
        return false;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire rd = (ReflectionDesire) desire;
        
        ClassSatisfaction satisfaction = null;
        if (Types.isInstantiable(implType)) {
            TypeAssignment assignment = Types.findCompatibleAssignment(implType, rd.getDesiredType());
            satisfaction = new ClassSatisfaction(implType, assignment);
        }
        
        // The default source is set to TYPE so that @ImplementedBy and @ProvidedBy
        // on the impl type can be followed, but any role defaults on the injection
        // point will be disabled.
        return new ReflectionDesire(implType, rd.getInjectionPoint(), 
                                    satisfaction, DefaultSource.TYPE);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TypeBindRule)) {
            return false;
        }
        return super.equals(o) && ((TypeBindRule) o).implType.equals(implType);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ implType.hashCode();
    }
    
    @Override
    public String toString() {
        if (getRole() == null) {
            return "TypeBindRule(" + getSourceType() + " -> " + implType + ")";
        } else {
            return "TypeBindRule(" + getRole() + ":" + getSourceType() + " -> " + implType + ")";
        }
    }
}
