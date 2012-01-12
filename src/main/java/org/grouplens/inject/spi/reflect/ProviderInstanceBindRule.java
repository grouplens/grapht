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

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

public class ProviderInstanceBindRule<T> extends ReflectionBindRule {
    private final Provider<? extends T> provider;
    
    // FIXME: I think we can move a lot of the bind rule implementations into
    // a single class BindRules with static methods to return implementations for
    // the different types that we need
    // FIXME: can the same be done for satisfactions? Is it worth it?
    
    public ProviderInstanceBindRule(Provider<? extends T> provider, AnnotationRole role, Class<? super T> sourceType, boolean generated) {
        super(role, sourceType, generated);
        if (provider == null) {
            throw new NullPointerException("Provider instance cannot be null");
        }
        // FIXME: verify that the provider provides objects that extend from sourceType
        this.provider = provider;
    }

    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire origDesire = (ReflectionDesire) desire;
        ProviderInstanceSatisfaction satisfaction = new ProviderInstanceSatisfaction(provider);
        return new ReflectionDesire(satisfaction.getErasedType(), origDesire.getInjectionPoint(), satisfaction);
    }

    // FIXME: document and implement equals/hashcode
}
