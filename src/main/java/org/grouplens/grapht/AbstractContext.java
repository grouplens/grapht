package org.grouplens.grapht;

import java.lang.annotation.Annotation;

/**
 * Implementations of convenience methods on {@link Context}.
 */
public abstract class AbstractContext implements Context {
    @Override
    public <T> Binding<T> bind(Class<? extends Annotation> qual,
                               Class<T> type) {
        return bind(type).withQualifier(qual);
    }
}
