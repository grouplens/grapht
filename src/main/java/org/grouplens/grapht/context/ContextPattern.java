/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.grapht.context;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.solver.InjectionContext;
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
     * Create a context matcher matching any context with a subequence matching the provided matchers.
     *
     * @param matchers The matchers to match a subsequence.
     * @return A context matcher that matches any context of which {@code types} (with the default
     *         qualifier matchers) is a subsequence.
     */
    public static ContextPattern subsequence(ContextElementMatcher... matchers) {
        ContextPattern pat = any();
        for (ContextElementMatcher matcher: matchers) {
            pat = pat.append(matcher, Multiplicity.ONE)
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
    public ContextPattern append(ContextElementMatcher match, Multiplicity mult) {
        Element elem = new Element(match, mult);
        Chain<Element> chain = tokenChain.extend(elem);
        return new ContextPattern(chain);
    }

    /**
     * Append an element to this pattern with multiplicity {@link Multiplicity#ONE}.
     * @param match The element matcher.
     * @return This pattern extended by the new element.
     */
    public ContextPattern append(ContextElementMatcher match) {
        return append(match, Multiplicity.ONE);
    }

    /**
     * Append a type element to this pattern with multiplicity {@link Multiplicity#ONE}.
     * @param type The type to match, passed to {@link ContextElements#matchType(Class)}.
     * @return This pattern extended by the new element.
     */
    public ContextPattern append(Class<?> type) {
        return append(ContextElements.matchType(type));
    }

    /**
     * Append a context pattern to this pattern.
     * @param toAppend The pattern to append.
     * @return The concatenation of this pattern and {@code toAppend}.
     */
    public ContextPattern append(ContextPattern toAppend) {
        ContextPattern pat = this;
        for (Element elem: toAppend.tokenChain) {
            pat = pat.append(elem.getMatcher(), elem.getMultiplicity());
        }
        return pat;
    }

    /**
     * Return a pattern matching any context of which the current pattern matches a prefix.  This
     * is accomplished by adding the {@code .*} regular expression token.
     * @return The new pattern.
     */
    public ContextPattern appendDotStar() {
        if (!tokenChain.isEmpty()) {
            Element elem = tokenChain.getTailValue();
            if (elem.getMatcher().equals(ContextElements.matchAny()) && elem.getMultiplicity().equals(Multiplicity.ZERO_OR_MORE)) {
                return this;
            }
        }

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
        Pair<Satisfaction,InjectionPoint> ctxElem = context.getTailValue();
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
    public String toString() {
        return tokenChain.toString();
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Element element = (Element) o;

            if (!matcher.equals(element.matcher)) return false;
            if (multiplicity != element.multiplicity) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = matcher.hashCode();
            result = 31 * result + multiplicity.hashCode();
            return result;
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
