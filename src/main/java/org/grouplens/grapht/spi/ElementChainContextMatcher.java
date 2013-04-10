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
package org.grouplens.grapht.spi;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.spi.reflect.ReflectionContextElementMatcher;
import org.grouplens.grapht.util.Types;

import java.io.*;
import java.util.*;

/**
 * ElementChainContextMatcher represents a list of {@link ContextElementMatcher}s.
 * Context element matchers can match a single node within a context, and an
 * ElementChainContextMatcher can match an entire context. An
 * ElementChainContextMatcher matches a context if its element matchers match a
 * subsequence of the nodes within the context.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ElementChainContextMatcher implements ContextMatcher, Serializable {
    private static final long serialVersionUID = 1L;

    // FIXME Deserialize element matchers correctly
    private final List<ContextElementMatcher> elementMatchers;

    /**
     * Create a new ElementChainContextMatcher representing the empty context without any
     * matchers.
     */
    public ElementChainContextMatcher() {
        this(new ArrayList<ContextElementMatcher>());
    }

    /**
     * Create a new ElementChainContextMatcher with the given context matchers. The var-args
     * parameter forms a list in the order the arguments are passed in.
     * Arguments should not be null.
     * 
     * @param elementMatchers The var-args of matchers to use in this chain
     */
    public ElementChainContextMatcher(ContextElementMatcher... elementMatchers) {
        this(Arrays.asList(elementMatchers));
    }
    
    /**
     * Create a new ElementChainContextMatcher that matches the given sequence of
     * ContextMatchers. The list is copied so the created ElementChainContextMatcher is
     * immutable. The list should not contain any null elements.
     * 
     * @param matchers The matcher list this chain represents
     * @throws NullPointerException if matchers is null
     */
    public ElementChainContextMatcher(List<? extends ContextElementMatcher> matchers) {
        this.elementMatchers = new ArrayList<ContextElementMatcher>(matchers);
    }
    
    /**
     * @return An unmodifiable list of the context matchers in this chain
     */
    public List<ContextElementMatcher> getContexts() {
        return Collections.unmodifiableList(elementMatchers);
    }

    /**
     * <p>
     * Return whether or not this chain of ContextMatchers matches the actual
     * context represented by the ordered list of Satisfaction and Roles. The
     * start of the context represents the root of the dependency path.
     * <p>
     * This returns true if the sequence of context elementMatchers is a subsequence of
     * the contexts, with respect to the matcher's
     * {@link ContextElementMatcher#matches(Pair) match()} method.
     * <p>
     * Given this definition, a ElementChainContextMatcher with no elementMatchers will match every
     * real context.
     * 
     * @param context The current context
     * @return True if this chain matches
     */
    @Override
    public ContextMatch matches(InjectionContext context) {
        List<Pair<Satisfaction, Attributes>> nodes = context.getTypePath();

        /* Walk backwards through both lists, scanning for a match. At the end,
         * we'll check the anchor counters to see if there was one. */
        // matched element indexes, reversed (matches[0] is index of match of last element matcher).
        int[] matches = new int[elementMatchers.size()];
        // ei is the index of the current matcher. also, number of unmatched matchers - 1
        int ei = elementMatchers.size() - 1;
        // ni is the index of the current noe
        int ni = nodes.size() - 1;
        // step very, very carefully
        while (ni >= 0 && ei >= 0) {
            ContextElementMatcher em = elementMatchers.get(ei);
            if (em.matches(nodes.get(ni))) {
                // we have a match, advance both counters
                matches[matches.length - ei - 1] = ni;
                ei--;
                ni--;
            } else if (em.isAnchored()) {
                // no match, but em is anchored. So skip the rest of the nodes, no match.
                ni = -1;
            } else {
                // no match,but em is not anchored. therefore, check next node
                ni--;
            }
        }

        if (ei < 0) {
            // we matched all matchers
            return new Match(elementMatchers, context, matches);
        } else {
            return null;
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ElementChainContextMatcher)) {
            return false;
        } else {
            return ((ElementChainContextMatcher) o).elementMatchers.equals(elementMatchers);
        }
    }
    
    @Override
    public int hashCode() {
        return elementMatchers.hashCode();
    }
    
    @Override
    public String toString() {
        return "ElementChainContextMatcher(" + elementMatchers.toString() + ")";
    }

    private static class Match implements ContextMatch {
        private List<ContextElementMatcher> matchers;
        private InjectionContext context;
        private int[] matches;

        /**
         * Construct a new match.
         *
         * @param eltMatchers The matchers.
         * @param ctx The matched context.
         * @param ms The indexes of the element matches, in reverse order.
         */
        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        private Match(List<ContextElementMatcher> eltMatchers, InjectionContext ctx, int[] ms) {
            if (eltMatchers.size() != ms.length) {
                throw new IllegalArgumentException("mismatched match array");
            }
            matchers = eltMatchers;
            context = ctx;
            matches = ms;
        }

        /**
         * Compare two matches.
         *
         * @param other The other matcher. Must be an element chain matcher of the same context.
         * @return The comparison result.
         */
        @Override
        public int compareTo(ContextMatch other) {
            // FIXME Make this work with non-chain matchers
            if (!(other instanceof Match)) {
                throw new IllegalArgumentException("cannot compare across matcher types");
            }

            Match m = (Match) other;

            if (!m.context.equals(context)) {
                throw new IllegalArgumentException("cannot compare across matched contexts");
            }

            CompareToBuilder ctb = new CompareToBuilder();

            // compare by closeness and length, reversed
            // if m.matches is greater, it is closer to end, so we want it first
            // if m.matches is shorter, it is lower priority, so we want this first
            ctb.append(m.matches, matches);

            if (ctb.toComparison() != 0) {
                return ctb.toComparison();
            }

            List<Pair<Satisfaction, Attributes>> path = context.getTypePath();

            // compare matcher type closeness from end to beginning
            assert matches.length == m.matches.length; // or we have problems
            for (int i = matches.length - 1; i >= 0; i--) {
                int mi = matches.length - i - 1;
                assert matches[mi] == m.matches[mi];

                ContextElementMatcher mine = matchers.get(i);
                ContextElementMatcher theirs = matchers.get(i);
                // and they both should jolly well match
                assert mine.matches(path.get(matches[mi]));
                assert theirs.matches(path.get(m.matches[mi]));

                Class<?> type = path.get(matches[mi]).getLeft().getErasedType();
                ctb.append(mine, theirs, new ElementMatcherComparator(type));
            }

            return ctb.toComparison();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof Match) {
                Match om = (Match) o;
                EqualsBuilder eqb = new EqualsBuilder();
                // matches is derived from matchers and context
                return eqb.append(matchers, om.matchers)
                          .append(context, om.context)
                          .isEquals();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            // matches is derived from matchers and context
            return hcb.append(matchers)
                      .append(context)
                      .build();
        }
    }

    private static class ElementMatcherComparator implements Comparator<ContextElementMatcher> {
        private final Class<?> type;

        public ElementMatcherComparator(Class<?> type) {
            this.type = type;
        }

        @Override
        public int compare(ContextElementMatcher o1, ContextElementMatcher o2) {
            ReflectionContextElementMatcher cm1 = (ReflectionContextElementMatcher) o1;
            ReflectionContextElementMatcher cm2 = (ReflectionContextElementMatcher) o2;

            // #1 - order by type distance, select the matcher that is closest
            int td1 = Types.getTypeDistance(type, cm1.getMatchedType());
            int td2 = Types.getTypeDistance(type, cm2.getMatchedType());
            if (td1 != td2) {
                return td1 - td2;
            }

            // #2 - order by qualifier priority
            return cm1.getMatchedQualifier().compareTo(cm2.getMatchedQualifier());
        }
    }
}
