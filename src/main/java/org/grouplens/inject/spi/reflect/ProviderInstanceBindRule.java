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
