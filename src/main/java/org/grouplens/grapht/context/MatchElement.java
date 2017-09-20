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

import com.google.common.collect.Ordering;

import org.jetbrains.annotations.Nullable;
import java.util.Comparator;

/**
 * A single element in a context match.  Used in {@link ContextMatch}.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public interface MatchElement {
    /**
     * Get the priority of this element matcher.
     *
     * @return The element matcher's priority.
     */
    ContextElements.MatchPriority getPriority();

    /**
     * Get the type distance of this match.
     *
     * @return The type distance in this match, or empty if the type distance is irrelevant
     *         for this type of matcher.
     */
    @Nullable
    Integer getTypeDistance();

    /**
     * Orderings for the match order.
     */
    public static enum Order implements Comparator<MatchElement> {
        /**
         * Priority-only ordering, used for first-pass comparison of context matchers.
         */
        PRIORITY_ONLY {
            @Override
            public int compare(MatchElement e1, MatchElement e2) {
                return e1.getPriority().compareTo(e2.getPriority());
            }
        },
        /**
         * Full ordering of match elements, including type distance.  Closer compares lower (higher
         * priority).
         */
        PRIORITY_AND_DISTANCE {
            @Override
            public int compare(MatchElement e1, MatchElement e2) {
                int cmp = e1.getPriority().compareTo(e2.getPriority());
                if (cmp == 0) {
                    Ordering<Integer> distOrder = Ordering.<Integer>natural().nullsLast();
                    cmp = distOrder.compare(e1.getTypeDistance(), e2.getTypeDistance());
                }
                return cmp;
            }
        }
    }
}
