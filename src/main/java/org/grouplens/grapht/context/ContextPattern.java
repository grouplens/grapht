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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.util.AbstractChain;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * A regular pattern matching contexts.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ContextPattern implements ContextMatcher, Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Element> tokenChain;

    private ContextPattern() {
        tokenChain = Collections.emptyList();
    }

    private ContextPattern(List<Element> tokens) {
        tokenChain = ImmutableList.copyOf(tokens);
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
        List<Element> extended = ImmutableList.<Element>builder()
                                              .addAll(tokenChain)
                                              .add(elem)
                                              .build();
        return new ContextPattern(extended);
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
            Element elem = tokenChain.get(tokenChain.size() - 1);
            if (elem.getMatcher().equals(ContextElements.matchAny()) && elem.getMultiplicity().equals(Multiplicity.ZERO_OR_MORE)) {
                return this;
            }
        }

        return append(ContextElements.matchAny(), Multiplicity.ZERO_OR_MORE);
    }

    @Override
    public ContextMatch matches(InjectionContext context) {
        List<MatchElement> result = recursiveMatch(tokenChain, ImmutableList.copyOf(context));
        if (result == null) {
            return null;
        } else {
            return ContextMatch.create(result);
        }
    }

    /**
     * Recursive matching routine.  Matches the pattern via backtracking.  Returns the matched
     * elements.
     *
     * @param pattern The pattern.
     * @param context The context.
     * @return The chain of match elements (in reverse order).
     */
    private List<MatchElement> recursiveMatch(List<Element> pattern, List<Pair<Satisfaction, InjectionPoint>> context) {
        if (pattern.isEmpty()) {
            if (context.isEmpty()) {
                return Collections.emptyList();
            } else {
                return null;
            }
        }

        Element element = listHead(pattern);

        // matching against the empty string with a nonempty pattern
        if (context.isEmpty()) {
            if (element.getMultiplicity().isOptional()) {
                return recursiveMatch(pattern.subList(1, pattern.size()), context);
            } else {
                return null;
            }
        }
        // non-empty pattern, non-empty context, go
        Pair<Satisfaction,InjectionPoint> ctxElem = listHead(context);
        MatchElement match = element.getMatcher().apply(ctxElem);
        if (match == null) {
            // no match, what do we do?
            if (element.getMultiplicity().isOptional()) {
                // skip this element, keep going
                return recursiveMatch(listTail(pattern), context);
            } else {
                // oops, we must match
                return null;
            }
        } else {
            // we have a match, try recursion
            List<Element> nextPat = pattern;
            if (element.getMultiplicity().isConsumed()) {
                nextPat = listTail(nextPat);
            }
            List<MatchElement> result = recursiveMatch(nextPat, listTail(context));
            if (result == null && element.getMultiplicity().isOptional()) {
                // recursive match failed, but element is optional. Try again without it.
                return recursiveMatch(listTail(pattern), context);
            }
            if (result != null) {
                return ImmutableList.<MatchElement>builder()
                                    .add(match)
                                    .addAll(result)
                                    .build();
            } else {
                return result;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ContextPattern(");
        for (Element tok: tokenChain) {
            sb.append(tok);
        }
        return sb.append(")").toString();
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

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(matcher);
            switch (multiplicity) {
            case ONE:
                break;
            case ZERO_OR_MORE:
                sb.append("*");
                break;
            default:
                throw new IllegalStateException("unknown multiplicity");
            }
            return sb.toString();
        }
    }

    private static <E> E listHead(List<E> lst) {
        Preconditions.checkArgument(!lst.isEmpty(), "list cannot be empty");
        return lst.get(0);
    }

    private static <E> List<E> listTail(List<E> lst) {
        Preconditions.checkArgument(!lst.isEmpty(), "list cannot be empty");
        return lst.subList(1, lst.size());
    }
}
