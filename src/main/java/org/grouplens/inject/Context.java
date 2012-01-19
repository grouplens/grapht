package org.grouplens.inject;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;

public interface Context {
    <T> Binding<T> bind(Class<T> type);
    
    <T> Binding<T> bind(Class<T> type, Class<?>... otherTypes);

    Context in(Class<?> type);
    
    Context in(@Nullable Class<? extends Annotation> role, Class<?> type);
}
