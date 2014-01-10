package org.grouplens.grapht.spi.context;

import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.reflect.ReflectionContextElementMatcher;

/**
 * Utilities for context matching.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class ContextElements {
    private ContextElements() {}

    /**
     * Match priority constants.
     */
    public static enum MatchPriority {
        TYPE,
        INVERTED,
        ANY
    }

    public static ContextElementMatcher matchAny() {
        return null;
    }

    public static ContextElementMatcher matchType(Class<?> type) {
        return new ReflectionContextElementMatcher(type);
    }

    public static ContextElementMatcher matchType(Class<?> type,  QualifierMatcher qual) {
        return new ReflectionContextElementMatcher(type, qual);
    }

    public static ContextElementMatcher invertMatch(ContextElementMatcher matcher) {
        return null;
    }


}
