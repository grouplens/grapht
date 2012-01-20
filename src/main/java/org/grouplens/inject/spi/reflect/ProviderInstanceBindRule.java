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
import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

/**
 * ProviderInstanceBindRule is a reflection bind rule that satisfies all
 * matching desires with a {@link ProviderInstanceSatisfaction}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ProviderInstanceBindRule extends ReflectionBindRule {
    private final Provider<?> provider;

    /**
     * Create a ProviderInstanceBindRule that binds <tt>provider</tt> to the
     * given source type and role. An exception is thrown if the provider does
     * not provide instances of the source type.
     * 
     * @param provider The provider that will satisfy any desires matched by
     *            this bind rule
     * @param sourceType The source type this bind rule matches to
     * @param role The role this bind rule matches to
     * @param weight The weight of the rule
     * @throws NullPointerException if provider or sourceType are null
     * @throws IllegalArgumentException if the provider's created instances do
     *             not extend from sourceType
     */
    @SuppressWarnings("unchecked")
    public ProviderInstanceBindRule(Provider<?> provider, Class<?> sourceType, @Nullable AnnotationRole role, int weight) {
        super(sourceType, role, weight);
        if (provider == null) {
            throw new NullPointerException("Provider instance cannot be null");
        }
        if (!sourceType.isAssignableFrom(Types.getProvidedType((Class<? extends Provider<?>>) provider.getClass()))) {
            throw new IllegalArgumentException("Provider does not provide instances of " + sourceType);
        }
        
        this.provider = provider;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire origDesire = (ReflectionDesire) desire;
        ProviderInstanceSatisfaction satisfaction = new ProviderInstanceSatisfaction(provider);
        return new ReflectionDesire(satisfaction.getErasedType(), origDesire.getInjectionPoint(), satisfaction);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProviderInstanceBindRule)) {
            return false;
        }
        return super.equals(o) && ((ProviderInstanceBindRule) o).provider == provider;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode() ^ System.identityHashCode(provider);
    }
    
    @Override
    public String toString() {
        return "ProviderInstanceBindRule(" + getRole() + ":" + getSourceType() + " -> " + provider + ")";
    }
}
