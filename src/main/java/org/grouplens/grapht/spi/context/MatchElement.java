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

import com.google.common.collect.Ordering;

import javax.annotation.Nullable;
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
