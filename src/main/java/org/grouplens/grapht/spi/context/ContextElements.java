package org.grouplens.grapht.spi.context;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
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
        return AnyMatcher.INSTANCE;
    }

    public static ContextElementMatcher matchType(Class<?> type) {
        return new ReflectionContextElementMatcher(type);
    }

    public static ContextElementMatcher matchType(Class<?> type,  QualifierMatcher qual) {
        return new ReflectionContextElementMatcher(type, qual);
    }

    public static ContextElementMatcher invertMatch(ContextElementMatcher matcher) {
        return new InvertedMatcher(matcher);
    }

    private static enum AnyMatcher implements ContextElementMatcher {
        INSTANCE;

        @Override
        public MatchElement apply(Pair<Satisfaction, Attributes> n, int position) {
            return new ME();
        }

        private static class ME implements MatchElement {
            @Override
            public boolean includeInComparisons() {
                return false;
            }

            @Override
            public MatchPriority getPriority() {
                return MatchPriority.ANY;
            }

            @Override
            public int compareTo(MatchElement o) {
                return MatchPriority.ANY.compareTo(o.getPriority());
            }
        }
    }

    private static class InvertedMatcher implements ContextElementMatcher {
        private static final long serialVersionUID = 1L;

        private final ContextElementMatcher delegate;

        public InvertedMatcher(ContextElementMatcher base) {
            delegate = base;
        }

        @Override
        public MatchElement apply(Pair<Satisfaction, Attributes> elem, int position) {
            MatchElement result = delegate.apply(elem, position);
            if (result == null) {
                return new ME(position);
            } else {
                return null;
            }
        }

        private static class ME implements MatchElement {
            private final int position;

            public ME(int pos) {
                position = pos;
            }

            @Override
            public boolean includeInComparisons() {
                return true;
            }

            @Override
            public MatchPriority getPriority() {
                return MatchPriority.INVERTED;
            }

            @Override
            public int compareTo(MatchElement o) {
                CompareToBuilder ctb = new CompareToBuilder();
                ctb.append(MatchPriority.INVERTED, o.getPriority());
                if (ctb.toComparison() == 0) {
                    ME ome = (ME) o;
                    ctb.append(ome.position, position);
                }
                return ctb.toComparison();
            }
        }
    }
}
