package org.grouplens.grapht;

/**
 * Interface for instantiating components.  It functions much like {@link javax.inject.Provider},
 * except that it reports failure with a checked exception.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.9
 */
public interface Instantiator {
    Object instantiate() throws InjectionException;

    /**
     * Get the type that this instantiator will instantiate.
     * @return The type returned by this instantiator.
     */
    Class getType();
}
