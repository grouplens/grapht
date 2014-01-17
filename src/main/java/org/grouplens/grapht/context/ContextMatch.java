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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import java.util.List;

/**
 * Interface for context matches. A context match is the result of successfully
 * matching a {@link ContextMatcher}.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ContextMatch implements Comparable<ContextMatch> {
    private final ImmutableList<MatchElement> matchElements;

    private ContextMatch(List<MatchElement> matches) {
        matchElements = ImmutableList.copyOf(matches);
    }

    /**
     * Create a new context match.
     * @param matches The list of match elements.
     * @return The context match.
     */
    static ContextMatch create(List<MatchElement> matches) {
        return new ContextMatch(matches);
    }

    @Override
    public int compareTo(ContextMatch o) {
        return Ordering.from(MatchElement.Order.PRIORITY_ONLY)
                       .lexicographical()
                       .compound(Ordering.from(MatchElement.Order.PRIORITY_AND_DISTANCE)
                                         .lexicographical())
                       .compare(matchElements.reverse(), o.matchElements.reverse());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ContextMatch) {
            ContextMatch om = (ContextMatch) o;
            return matchElements.equals(om.matchElements);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return matchElements.hashCode();
    }

    @Override
    public String toString() {
        return "ContextMatch" + matchElements.toString();
    }
}
