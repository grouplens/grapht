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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.QualifierMatcher;

/**
 * BindRule is a partial function from desire to desire that acts as a binding.
 * The {@link RuleBasedBindingFunction} takes a collection of BindRules grouped
 * into their activating contexts to form a {@link BindingFunction}.
 *
 * @see {@link BindRules}
 */
public interface BindRule {
    /**
     * Get the rule's cache policy.
     * @return The CachePolicy to use for satisfactions created with this rule.
     */
    CachePolicy getCachePolicy();

    /**
     * Get the rule's qualifier matcher.
     * @return The annotation {@link QualifierMatcher} matched by this bind rule.
     */
    QualifierMatcher getQualifier();

    /**
     * Apply this BindRule to the given Desire, and return a restricted and
     * possibly satisfied desire. It is assumed that {@link #matches(org.grouplens.grapht.spi.Desire)}
     * returns true.
     *
     * @param desire The desire that is input to this partial binding function
     * @return The restricted desire
     */
    Desire apply(Desire desire);

    /**
     * Query whether this rule is terminal.  Terminal rules have no further lookup
     * applied in order to find the final binding target.
     *
     * @return True if this should be the last bind rule applied to the desire
     *         chain when attempting to find a satisfaction
     */
    boolean isTerminal();

    /**
     * @param desire The input desire
     * @return True if this desire matches this BindRule and can be passed to
     *         {@link #apply(org.grouplens.grapht.spi.Desire)}
     */
    boolean matches(Desire desire);
}
