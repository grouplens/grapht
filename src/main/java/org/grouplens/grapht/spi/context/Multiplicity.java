package org.grouplens.grapht.spi.context;

/**
 * Multiplicity of element matches - how many times may/must an element match?
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum Multiplicity {
    /**
     * Match exactly once.
     */
    ONE,
    /**
     * Match zero or more times.
     */
    ZERO_OR_MORE
}
