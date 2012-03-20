/*
 * LensKit, an open source recommender systems toolkit.
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

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.spi.reflect.ReflectionDesire.DefaultSource;
import org.grouplens.inject.types.Types;

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
     * source type and qualifier. An exception is thrown if the provider does not
     * provide instances of the source type.
     * 
     * @param providerType The Provider implementation to bind to
     * @param sourceType The source type matched by this bind rule
     * @param qualifier The qualifier matched by this bind rule
     * @param weight The weight of the rule
     * @throws NullPointerException if providerType or sourceType are null
     * @throws IllegalArgumentException if providerType does not provide
     *             implementations of sourceType
     */
    public ProviderClassBindRule(Class<? extends Provider<?>> providerType, Class<?> sourceType, 
                                 @Nullable Qualifier qualifier, int weight) {
        super(sourceType, qualifier, weight);
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
        // The NONE DefaultSource is used so that any time this bind rule is applied,
        // we know a default cannot be followed (which would effectively bypass the
        // provider class binding, which seems strange).
        return new ReflectionDesire(satisfaction.getErasedType(), rd.getInjectionPoint(), 
                                    satisfaction, DefaultSource.NONE);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProviderClassBindRule)) {
            return false;
        }
        return super.equals(o) && ((ProviderClassBindRule) o).providerType.equals(providerType);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ providerType.hashCode();
    }
    
    @Override
    public String toString() {
        return "ProviderClassBindRule(" + getWeight() + ", "  + getQualifier() + ":" + getSourceType() + " -> " + providerType + ")";
    }

    @Override
    public boolean terminatesChain() {
        return true;
    }
}
