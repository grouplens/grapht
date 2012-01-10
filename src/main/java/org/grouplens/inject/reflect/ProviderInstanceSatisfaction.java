package org.grouplens.inject.reflect;

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
class ProviderInstanceSatisfaction extends ReflectionSatisfaction {
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
    
    @Override
    public List<Desire> getDependencies() {
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
}
