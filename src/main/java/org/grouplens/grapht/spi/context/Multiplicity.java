package org.grouplens.grapht.spi.context;

/**
 * Multiplicity of element matches - how many times may/must an element match?
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum Multiplicity {
    /**
     * Match exactly once.
     */
    ONE {
        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public boolean isConsumed() {
            return true;
        }
    },
    /**
     * Match zero or more times.
     */
    ZERO_OR_MORE {
        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public boolean isConsumed() {
            return false;
        }
    };

    public abstract boolean isOptional();
    public abstract boolean isConsumed();
}
