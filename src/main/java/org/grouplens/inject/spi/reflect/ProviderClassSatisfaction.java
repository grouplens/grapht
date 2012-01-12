package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

/**
 * ProviderClassSatisfaction is a satisfaction implementation that satisfies a
 * type given a {@link Provider} class capable of providing that type.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class ProviderClassSatisfaction extends ReflectionSatisfaction {
    private final Class<? extends Provider<?>> providerType;

    /**
     * Create a ProviderClassSatisfaction that wraps a given provider type.
     * 
     * @param providerType The provider class type
     * @throws NullPointerException if providerType is null
     * @throws IllegalArgumentException if the class is not a Provider, or is not instantiable
     */
    public ProviderClassSatisfaction(Class<? extends Provider<?>> providerType) {
        if (providerType == null) {
            throw new NullPointerException("Provider class cannot be null");
        }
        if (Provider.class.isAssignableFrom(providerType)) {
            throw new IllegalArgumentException("Class type is not a Provider implementation");
        }
        if (!Types.isInstantiable(providerType)) {
            throw new IllegalArgumentException("Provider class cannot be instntiated");
        }
        
        this.providerType = providerType;
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Types.getDesires(providerType);
    }

    @Override
    public Type getType() {
        return Types.getProvidedType(providerType);
    }

    @Override
    public Class<?> getErasedType() {
        return Types.getProvidedType(providerType);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
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
}
