package org.grouplens.grapht.util;

import javax.inject.Provider;

/**
 * MemoizingProvider is a Provider that enforces memoization or caching on
 * another Provider that it wraps.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T>
 */
public class MemoizingProvider<T> implements Provider<T> {
    private final Provider<T> wrapped;
    
    // We track a boolean because this supports providing null instances, in
    // which case we can't just check against null to see if we've already
    // queried the base provider
    private T cached;
    private boolean invoked;
    
    public MemoizingProvider(Provider<T> provider) {
        Preconditions.notNull("provider", provider);
        wrapped = provider;
        cached = null;
        invoked = false;
    }
    
    @Override
    public T get() {
        if (!invoked) {
            cached = wrapped.get();
            invoked = true;
        }
        return cached;
    }
}