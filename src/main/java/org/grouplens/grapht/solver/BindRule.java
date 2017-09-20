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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.reflect.Desire;

import java.util.EnumSet;

/**
 * BindRule is a partial function from desire to desire that acts as a binding.
 * The {@link RuleBasedBindingFunction} takes a collection of BindRules grouped
 * into their activating contexts to form a {@link BindingFunction}.
 *
 * <p><b>Note:</b> this interface's ordering is inconsistent with {@link Object#equals(Object)}. See
 * {@link #compareTo(BindRule)} for more details.
 *
 * @see BindRules
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
     * Get the flags attached to this bind rule.
     *
     * @return The flags attached to the bind rule.
     */
    EnumSet<BindingFlag> getFlags();

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
     * <p><b>Note:</b> This implements an ordering inconsistent with {@link Object#equals(Object)}. Equal
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
