package org.grouplens.grapht.spi.context;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface MatchElement extends Comparable<MatchElement> {
    /**
     * Query if this element should be included when comparing context matches.
     * @return {@code true} if this match should be included, {@code false} if it should be ignored.
     */
    boolean includeInComparisons();

    /**
     * Get the priority of this element matcher.
     * @return The element matcher's priority.
     */
    ContextElements.MatchPriority getPriority();
}
