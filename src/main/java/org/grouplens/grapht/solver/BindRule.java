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

import org.grouplens.grapht.reflect.CachePolicy;
import org.grouplens.grapht.reflect.Desire;

/**
 * BindRule is a partial function from desire to desire that acts as a binding.
 * The {@link RuleBasedBindingFunction} takes a collection of BindRules grouped
 * into their activating contexts to form a {@link BindingFunction}.
 *
 * <p><b>Note:</b> this interface's ordering is inconsistent with {@link #equals(Object)}. See
 * {@link #compareTo(BindRule)} for more details.
 *
 * @see {@link BindRules}
 */
public interface BindRule extends Comparable<BindRule> {
    /**
     * Get the rule's cache policy.
     * @return The CachePolicy to use for satisfactions created with this rule.
     */
    CachePolicy getCachePolicy();

    /**
     * Apply this BindRule to the given Desire, and return a restricted and
     * possibly satisfied desire. It is assumed that {@link #matches(org.grouplens.grapht.reflect.Desire)}
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
     *         {@link #apply(org.grouplens.grapht.reflect.Desire)}
     */
    boolean matches(Desire desire);

    /**
     * Create a new bind rule builder initialized to copy this bind rule.  Use this to create a copy
     * of this bind rule that differs in some way.
     *
     * @return A new bind rule builder whose {@link BindRuleBuilder#build()} method will return a
     *         copy of this bind rule.
     * @throws UnsupportedOperationException if the bind rule cannot be reconfigured in this manner.
     */
    BindRuleBuilder newCopyBuilder();

    /**
     * Compare this bind rule to another. More-specific bind rules compare less than less-specific
     * rules.
     *
     * <p><b>Note:</b> This implements an ordering inconsistent with {@link #equals(Object)}. Equal
     * rules will compare the same (and implementations are required to enforce this), but unequal
     * rules may compare equal to each other when compared with this method.
     *
     * @param other The bind rule to compare with.
     * @return The comparison result.
     * @throws IllegalArgumentException if called with an incompatible bind rule.  This should not
     *                                  arise in practice, but if you implement your own bind rules
     *                                  they will not be comparable with the ones provided by
     *                                  Grapht.
     */
    @Override
    int compareTo(BindRule other);
}
