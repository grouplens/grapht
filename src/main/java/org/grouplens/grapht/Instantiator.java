package org.grouplens.grapht;

import java.util.concurrent.Callable;

/**
 * Interface for instantiating components.  It functions much like {@link javax.inject.Provider},
 * except that it reports failure with a checked exception.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.9
 */
public interface Instantiator extends Callable<Object> {
    Object call() throws InjectionException;

    /**
     * Get the type that this instantiator will instantiate.
     * @return The type returned by this instantiator.
     */
    Class getType();
}
