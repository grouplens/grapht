package org.grouplens.inject.util;

import javax.inject.Provider;

/**
 * InstanceProvider is a simple Provider that always provides the same instance.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T>
 */
public class InstanceProvider<T> implements Provider<T> {
    private final T instance;
    
    public InstanceProvider(T instance) {
        this.instance = instance;
    }
    
    @Override
    public T get() {
        return instance;
    }
}
