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
import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

/**
 * ProviderClassBindRule is a bind rule between a type and a Provider class
 * type. It satisfies all matching desires with a
 * {@link ProviderClassSatisfaction}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ProviderClassBindRule extends ReflectionBindRule {
    private final Class<? extends Provider<?>> providerType;

    /**
     * Create a ProviderClassBindRule that binds the given Provider type to the
     * source type and role. An exception is thrown if the provider does not
     * provide instances of the source type.
     * 
     * @param providerType The Provider implementation to bind to
     * @param sourceType The source type matched by this bind rule
     * @param role The role matched by this bind rule
     * @param generated True if this rule was automatically generated
     * @throws NullPointerException if providerType or sourceType are null
     * @throws IllegalArgumentException if providerType does not provide
     *             implementations of sourceType
     */
    public ProviderClassBindRule(Class<? extends Provider<?>> providerType, Class<?> sourceType, @Nullable AnnotationRole role, boolean generated) {
        super(sourceType, role, generated);
        if (providerType == null) {
            throw new NullPointerException("Provider type cannot be null");
        }
        if (!sourceType.isAssignableFrom(Types.getProvidedType(providerType))) {
            throw new IllegalArgumentException("Provider type does not provide instances of " + sourceType);
        }
        
        this.providerType = providerType;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire rd = (ReflectionDesire) desire;
        ProviderClassSatisfaction satisfaction = new ProviderClassSatisfaction(providerType);
        return new ReflectionDesire(satisfaction.getErasedType(), rd.getInjectionPoint(), satisfaction);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProviderClassBindRule)) {
            return false;
        }
        return ((ProviderClassBindRule) o).providerType.equals(providerType);
    }
    
    @Override
    public int hashCode() {
        return providerType.hashCode();
    }
}
