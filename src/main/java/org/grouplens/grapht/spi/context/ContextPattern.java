package org.grouplens.grapht.spi.context;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.AbstractChain;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * A regular pattern matching contexts.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ContextPattern implements ContextMatcher, Serializable {
    private static final long serialVersionUID = 1L;

    private final Chain<Element> tokenChain;

    private ContextPattern() {
        tokenChain = new Chain<Element>();
    }

    private ContextPattern(Chain<Element> tokens) {
        if (tokens == null) {
            tokenChain = new Chain<Element>();
        } else {
            tokenChain = tokens;
        }
    }

    /**
     * Create an empty context pattern.
     * @return A context pattern matching only the empty context.
     */
    public static ContextPattern empty() {
        return new ContextPattern();
    }

    /**
     * Create a context pattern matching any context.
     * @return A context pattern matching any context.
     */
    public static ContextPattern any() {
        return empty().appendDotStar();
    }

    /**
     * Create a context matcher matching any context with the specified subsequence.
     *
     * @param types The subsequence to match.
     * @return A context matcher that matches any context of which {@code types} (with the default
     *         qualifier matchers) is a subsequence.
     */
    public static ContextPattern subsequence(Class<?>... types) {
        ContextPattern pat = any();
        for (Class<?> type: types) {
            pat = pat.append(ContextElements.matchType(type), Multiplicity.ONE)
                     .appendDotStar();
        }
        return pat;
    }

    /**
     * Append an element to this pattern.  The pattern is not modified; rather, a new pattern
     * extended with the new matching element is returned.
     * @param match The element matcher.
     * @param mult The multiplicity of this element.
     * @return This pattern extended by the new element.
     */
    ContextPattern append(ContextElementMatcher match, Multiplicity mult) {
        Element elem = new Element(match, mult);
        Chain<Element> chain = tokenChain.extend(elem);
        return new ContextPattern(chain);
    }

    /**
     * Append an element to this pattern with multiplicity {@link Multiplicity#ONE}.
     * @param match The element matcher.
     * @return This pattern extended by the new element.
     */
    ContextPattern append(ContextElementMatcher match) {
        return append(match, Multiplicity.ONE);
    }

    /**
     * Append a type element to this pattern with multiplicity {@link Multiplicity#ONE}.
     * @param type The type to match, passed to {@link ContextElements#matchType(Class)}.
     * @return This pattern extended by the new element.
     */
    ContextPattern append(Class<?> type) {
        return append(ContextElements.matchType(type));
    }

    /**
     * Return a pattern matching any context of which the current pattern matches a prefix.  This
     * is accomplished by adding the {@code .*} regular expression token.
     * @return The new pattern.
     */
    ContextPattern appendDotStar() {
        return append(ContextElements.matchAny(), Multiplicity.ZERO_OR_MORE);
    }

    @Override
    public ContextMatch matches(InjectionContext context) {
        Chain<MatchElement> result = recursiveMatch(tokenChain, context);
        if (result == null) {
            return null;
        } else {
            return ContextMatch.create(result);
        }
    }

    /**
     * Recursive matching routine.  Matches the pattern via backtracking.  Returns the matched
     * elements.
     * @param pattern The pattern.
     * @param context The context.
     * @return The chain of match elements.
     */
    private Chain<MatchElement> recursiveMatch(Chain<Element> pattern, InjectionContext context) {
        if (pattern == null || pattern.isEmpty()) {
            if (context == null || context.isEmpty()) {
                return new Chain<MatchElement>();
            } else {
                return null;
            }
        } else if (context == null || context.isEmpty()) {
            if (pattern.getTailValue().getMultiplicity().isOptional()) {
                return recursiveMatch(pattern.getLeading(), context);
            } else {
                return null;
            }
        }
        // non-empty pattern, non-empty context, go
        Element matcher = pattern.getTailValue();
        Pair<Satisfaction, Attributes> ctxElem = context.getTailValue();
        MatchElement match = matcher.getMatcher().apply(ctxElem, context.size() - 1);
        if (match == null) {
            // no match, what do we do?
            if (matcher.getMultiplicity().isOptional()) {
                // skip this element, keep going
                return recursiveMatch(pattern.getLeading(), context);
            } else {
                // oops, we must match
                return null;
            }
        } else {
            // we have a match, try recursion
            Chain<Element> nextPat =
                    matcher.getMultiplicity().isConsumed() ? pattern.getLeading() : pattern;
            Chain<MatchElement> result = recursiveMatch(nextPat,
                                                        context.getLeading());
            if (result == null && matcher.getMultiplicity().isOptional()) {
                // recursive match failed, but element is optional. Try again without it.
                return recursiveMatch(pattern.getLeading(), context);
            }
            if (result != null) {
                return result.extend(match);
            } else {
                return result;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextPattern that = (ContextPattern) o;

        if (!tokenChain.equals(that.tokenChain)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return tokenChain.hashCode();
    }

    /**
     * An element of the context pattern.
     */
    public static class Element implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ContextElementMatcher matcher;
        private final Multiplicity multiplicity;

        public Element(ContextElementMatcher mat, Multiplicity mult) {
            matcher = mat;
            multiplicity = mult;
        }

        public ContextElementMatcher getMatcher() {
            return matcher;
        }

        public Multiplicity getMultiplicity() {
            return multiplicity;
        }
    }

    /**
     * A chain of context pattern elements.
     */
    private static class Chain<E> extends AbstractChain<E> {
        private static final long serialVersionUID = 1L;
        public Chain() {
            super();
        }

        private Chain(Chain<E> head, E val) {
            super(head, val);
        }

        public Chain<E> extend(E elem) {
            return new Chain<E>(this, elem);
        }

        @Nullable
        public Chain<E> getLeading() {
            return (Chain<E>) previous;
        }
    }
}
