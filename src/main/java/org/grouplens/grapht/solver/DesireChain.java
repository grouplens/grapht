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

import com.google.common.base.Predicate;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.util.AbstractChain;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    @Nonnull
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
    private DesireChain(DesireChain prev, @Nonnull Desire d) {
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

    @Nonnull
    public Desire getCurrentDesire() {
        return tailValue;
    }

    @Nonnull
    public Desire getInitialDesire() {
        return initialDesire;
    }

    /**
     * Return the list of desires up to, but not including, the current desire.
     * @return The previous desire chain.
     */
    @Nonnull
    public List<Desire> getPreviousDesires() {
        if (previous == null) {
            return Collections.emptyList();
        } else {
            return previous;
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
    @Nonnull
    public DesireChain extend(@Nonnull Desire d) {
        return new DesireChain(this, d);
    }
}
