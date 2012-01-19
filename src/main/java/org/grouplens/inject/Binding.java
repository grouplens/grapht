package org.grouplens.inject;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

public interface Binding<T> {
    Binding<T> withRole(Class<? extends Annotation> role);
    
    Binding<T> exclude(Class<?> exclude);
    
    Binding<T> cachePolicy(CachePolicy policy);
    
    void to(Class<? extends T> impl);
    
    void to(T instance);
    
    void toProvider(Class<? extends Provider<? extends T>> provider);
    
    void toProvider(Provider<? extends T> provider);
}
