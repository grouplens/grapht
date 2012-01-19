package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.InjectSPI;

public class ReflectionInjectSPI implements InjectSPI {
    @Override
    public <T> BindRule bind(Class<? extends Annotation> role, Class<T> source,
                             Class<? extends T> impl, int weight) {
        return new ClassBindRule(impl, source, role(role), weight);
    }

    @Override
    public <T> BindRule bind(Class<? extends Annotation> role, Class<T> source, T instance, int weight) {
        return new InstanceBindRule(instance, source, role(role), weight);
    }

    @Override
    public <T> BindRule bindProvider(Class<? extends Annotation> role, Class<T> source,
                                     Class<? extends Provider<? extends T>> providerType, int weight) {
        return new ProviderClassBindRule(providerType, source, role(role), weight);
    }

    @Override
    public <T> BindRule bindProvider(Class<? extends Annotation> role, Class<T> source,
                                     Provider<? extends T> provider, int weight) {
        return new ProviderInstanceBindRule(provider, source, role(role), weight);
    }

    @Override
    public ContextMatcher context(Class<? extends Annotation> role, Class<?> type) {
        return new ReflectionContextMatcher(type, role(role));
    }
    
    private AnnotationRole role(Class<? extends Annotation> role) {
        return (role == null ? null : new AnnotationRole(role));
    }
}
