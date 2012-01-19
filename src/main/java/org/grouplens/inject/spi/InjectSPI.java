package org.grouplens.inject.spi;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Provider;


public interface InjectSPI {
    <T> BindRule bind(@Nullable Class<? extends Annotation> role, Class<T> source, 
                      Class<? extends T> impl, int weight);
    
    <T> BindRule bind(@Nullable Class<? extends Annotation> role, Class<T> source,
                      T instance, int weight);
    
    <T> BindRule bindProvider(@Nullable Class<? extends Annotation> role, Class<T> source, 
                              Class<? extends Provider<? extends T>> providerType, int weight);
    
    <T> BindRule bindProvider(@Nullable Class<? extends Annotation> role, Class<T> source, 
                              Provider<? extends T> provider, int weight);
    
    ContextMatcher context(@Nullable Class<? extends Annotation> role, Class<?> type);
    
    // FIXME: Do I need to add desires and satisfaction handling here?
}
