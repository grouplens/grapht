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
package org.grouplens.grapht.spi.reflect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;
import javax.inject.Singleton;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * ProviderClassSatisfaction is a satisfaction implementation that satisfies a
 * type given a {@link Provider} class capable of providing that type.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ProviderClassSatisfaction implements Satisfaction, Externalizable {
    // "final"
    private Class<? extends Provider<?>> providerType;

    /**
     * Create a ProviderClassSatisfaction that wraps a given provider type.
     * 
     * @param providerType The provider class type
     * @throws NullPointerException if providerType is null
     * @throws IllegalArgumentException if the class is not a Provider, or is not instantiable
     */
    public ProviderClassSatisfaction(Class<? extends Provider<?>> providerType) {
        Preconditions.notNull("provider type", providerType);
        Preconditions.isAssignable(Provider.class, providerType);
        Preconditions.isInstantiable(providerType);
        
        this.providerType = providerType;
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public ProviderClassSatisfaction() { }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }
    
    /**
     * @return The Provider class that provides instances satisfying this
     *         satisfaction
     */
    public Class<? extends Provider<?>> getProviderType() {
        return providerType;
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return ReflectionDesire.getDesires(providerType);
    }

    @Override
    public Type getType() {
        return Types.getProvidedType(providerType);
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
    public boolean isNull() {
        return false;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Provider<?> makeProvider(ProviderSource dependencies) {
        // we have to use the raw type because we don't have enough information,
        // but we can assume correctly that it will build a provider
        Provider<Provider<?>> providerBuilder = new InjectionProviderImpl(providerType, getDependencies(), dependencies);
        return providerBuilder.get();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProviderClassSatisfaction)) {
            return false;
        }
        return ((ProviderClassSatisfaction) o).providerType.equals(providerType);
    }
    
    @Override
    public int hashCode() {
        return providerType.hashCode();
    }
    
    @Override
    public String toString() {
        return "Provider(" + providerType.getSimpleName() + ")";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        providerType = (Class<? extends Provider<?>>) Types.readClass(in);
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeClass(out, providerType);
    }
}
