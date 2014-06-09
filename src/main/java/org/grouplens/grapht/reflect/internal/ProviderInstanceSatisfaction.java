/*
 * Grapht, an open source dependency injector.
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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.Instantiators;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Satisfaction implementation wrapping an existing Provider instance. It has no
 * dependencies and it always returns the same Provider when
 * {@link #makeInstantiator(Map)} is invoked.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ProviderInstanceSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = 1L;
    private final Provider<?> provider;

    /**
     * Create a new satisfaction that wraps the given Provider instance.
     * 
     * @param provider The provider
     * @throws NullPointerException if provider is null
     */
    public ProviderInstanceSatisfaction(Provider<?> provider) {
        Preconditions.notNull("provider", provider);
        this.provider = provider;
    }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }
    
    /**
     * @return The provider instance returned by {@link #makeInstantiator(Map)}
     */
    public Provider<?> getProvider() {
        return provider;
    }
    
    @Override
    public List<Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return Types.getProvidedType(provider);
    }

    @Override
    public Class<?> getErasedType() {
        return Types.erase(getType());
    }

    @Override
    public boolean hasInstance() {
        return false;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitProviderInstance(provider);
    }

    @Override
    public Instantiator makeInstantiator(Map<Desire,Instantiator> dependencies) {
        return Instantiators.ofProvider(provider);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof ProviderInstanceSatisfaction)
               && ((ProviderInstanceSatisfaction) o).provider.equals(provider);
    }
    
    @Override
    public int hashCode() {
        return provider.hashCode();
    }
    
    @Override
    public String toString() {
        return "ProviderInstance(" + provider + ")";
    }
}
