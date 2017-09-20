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

import com.google.common.base.Predicate;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.util.AbstractChain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A sequence of desires.  When one desire is resolved, that resolution can be a desire that needs
 * further resolution.  These desires are accumulated in a desire chain.  Desire chains are
 * immutable; appending to one results in a new desire chain object pointing to the previous chain.
 * They form a reverse singly linked list.  The chain maintains O(1) access to both the initial and
 * current desires.
 *
 * <p>When iterating a desire chain the initial desire is first and the most recent desire is last.
 *
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DesireChain extends AbstractChain<Desire> {
    @NotNull
    private final Desire initialDesire;
    private final UUID key;

    public static DesireChain singleton(Desire desire) {
        return new DesireChain(null, desire);
    }

    /**
     * Create a new desire chain.
     * @param prev The previous chain.
     * @param d The desire.
     */
    private DesireChain(DesireChain prev, @NotNull Desire d) {
        super(prev, d);
        key = prev == null ? UUID.randomUUID() : prev.key;
        initialDesire = prev == null ? d : prev.getInitialDesire();
    }

    public static Predicate<DesireChain> hasInitialDesire(final Desire d) {
        return new Predicate<DesireChain>() {
            @Override
            public boolean apply(@Nullable DesireChain input) {
                return input != null && input.getInitialDesire().equals(d);
            }
        };
    }

    @NotNull
    public Desire getCurrentDesire() {
        return tailValue;
    }

    @NotNull
    public Desire getInitialDesire() {
        return initialDesire;
    }

    /**
     * Return the list of desires up to, but not including, the current desire.
     * @return The previous desire chain.
     */
    @NotNull
    public List<Desire> getPreviousDesires() {
        if (previous == null) {
            return Collections.emptyList();
        } else {
            return previous;
        }
    }

    /**
     * Return the desire chain up to, but not including, the current desire.
     * @return The previous desire chain.
     */
    @NotNull
    public DesireChain getPreviousDesireChain() {
        if (previous == null) {
            throw new IllegalArgumentException("cannot get previous chain from singleton");
        } else {
            return (DesireChain) previous;
        }
    }

    /**
     * Get this chain's key. Each chain has a key, a unique object that is created when the chain
     * is created (via {@link #singleton(org.grouplens.grapht.reflect.Desire)}), and preserved through
     * {@link #extend(org.grouplens.grapht.reflect.Desire)} operations.  It can be used to remember
     * state across invocations of a binding function as a desire chain is built up.
     * @return The chain's key.
     */
    public Object getKey() {
        return key;
    }

    /**
     * Extend this chain with a new desire. The chain is not modified; this method returns a new
     * chain that includes the new desire as its current desire.
     *
     * @param d The new current desire.
     * @return The new desire chain.
     */
    @NotNull
    public DesireChain extend(@NotNull Desire d) {
        return new DesireChain(this, d);
    }
}
