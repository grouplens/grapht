/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
