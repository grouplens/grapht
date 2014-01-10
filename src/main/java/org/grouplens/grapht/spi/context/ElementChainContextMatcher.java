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
package org.grouplens.grapht.spi.context;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Satisfaction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ElementChainContextMatcher represents a list of {@link ContextElementMatcher}s.
 * Context element matchers can match a single node within a context, and an
 * ElementChainContextMatcher can match an entire context. An
 * ElementChainContextMatcher matches a context if its element matchers match a
 * subsequence of the nodes within the context.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ElementChainContextMatcher implements ContextMatcher, Serializable {
    private static final long serialVersionUID = 1L;

    private final ImmutableList<ContextElementMatcher> elementMatchers;

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
        this(ImmutableList.copyOf(elementMatchers));
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
        elementMatchers = ImmutableList.copyOf(matchers);
    }
    
    /**
     * @return An unmodifiable list of the context matchers in this chain
     */
    public List<ContextElementMatcher> getContexts() {
        return elementMatchers;
    }

    /**
     * <p>
     * Return whether or not this chain of ContextMatchers matches the actual
     * context represented by the ordered list of Satisfaction and Roles. The
     * start of the context represents the root of the dependency path.
     * <p>
     * This returns true if the sequence of context elementMatchers is a subsequence of
     * the contexts, with respect to the matcher's
     * {@link ContextElementMatcher#apply(Pair,int)} method.
     * <p>
     * Given this definition, a ElementChainContextMatcher with no elementMatchers will match every
     * real context.
     * 
     * @param context The current context
     * @return True if this chain matches
     */
    @Override
    public ContextMatch matches(InjectionContext context) {
        /* Walk backwards through both lists, scanning for a match. At the end,
         * we'll check the anchor counters to see if there was one. */

        // matches, reversed (matches.get(0) is the last match)
        List<MatchElement> matches = Lists.newLinkedList();

        // ei is the index of the current matcher. also, number of unmatched matchers - 1
        int ei = elementMatchers.size() - 1;
        int ni = context.size() - 1;
        Iterator<Pair<Satisfaction,Attributes>> iter = context.reverseIterator();
        while (iter.hasNext() && ei >= 0) {
            Pair<Satisfaction, Attributes> pair = iter.next();
            ContextElementMatcher em = elementMatchers.get(ei);
            MatchElement match = em.apply(pair, ni);
            if (match != null) {
                matches.add(0, match);
                ei--;
            }
            ni--;
        }

        if (ei < 0) {
            // we matched all matchers
            return ContextMatch.create(matches);
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
}
