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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

/**
 * Satisfaction implementation wrapping an existing Provider instance. It has no
 * dependencies and it always returns the same Provider when
 * {@link #makeProvider(Function)} is invoked.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ProviderInstanceSatisfaction extends ReflectionSatisfaction {
    private final Provider<?> provider;

    /**
     * Create a new satisfaction that wraps the given Provider instance.
     * 
     * @param provider The provider
     * @throws NullPointerException if provider is null
     */
    public ProviderInstanceSatisfaction(Provider<?> provider) {
        if (provider == null) {
            throw new NullPointerException("Provider cannot be null");
        }
        this.provider = provider;
    }
    
    /**
     * @return The provider instance returned by {@link #makeProvider(Function)}
     */
    public Provider<?> getProvider() {
        return provider;
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return getErasedType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<?> getErasedType() {
        return Types.getProvidedType((Class<? extends Provider<?>>) provider.getClass());
    }

    @Override
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return provider;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProviderInstanceSatisfaction)) {
            return false;
        }
        return ((ProviderInstanceSatisfaction) o).provider == provider;
    }
    
    @Override
    public int hashCode() {
        return System.identityHashCode(provider);
    }
}
