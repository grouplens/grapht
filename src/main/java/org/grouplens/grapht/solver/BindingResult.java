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
import org.grouplens.grapht.util.Preconditions;

import java.util.EnumSet;

/**
 * BindingResult is the result tuple of a {@link BindingFunction}. It is
 * effectively a {@link Desire} with additional metadata needed to implement
 * certain features within the dependency solver.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class BindingResult {
    private final Desire desire;
    private final CachePolicy policy;
    private final EnumSet<BindingFlag> flags;

    /**
     * Create a new result that wraps the given Desire.
     * 
     * @param desire The resultant desire from a BindingFunction
     * @param policy The CachePolicy for this binding
     * @throws NullPointerException if desire or policy is null
     */
    BindingResult(Desire desire, CachePolicy policy, EnumSet<BindingFlag> flags) {
        Preconditions.notNull("desire", desire);
        Preconditions.notNull("policy", policy);
        
        this.policy = policy;
        this.desire = desire;
        this.flags = flags.clone();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(Desire desire, CachePolicy policy) {
        return newBuilder()
                .setDesire(desire)
                .setCachePolicy(policy);
    }

    /**
     * @return The restricted desire result of the binding function
     */
    public Desire getDesire() {
        return desire;
    }
    
    /**
     * @return The CachePolicy for this binding
     */
    public CachePolicy getCachePolicy() {
        return policy;
    }

    /**
     * Query if the binding result is fixed.
     * @return {@code true} if the resulting satisfaction should refuse to be rewritten.
     */
    public boolean isFixed() {
        return flags.contains(BindingFlag.FIXED);
    }
    
    /**
     * @return True if the resulting desire should be deferred until all other
     *         desires in this phase have been completed
     */
    public boolean isDeferred() {
        return flags.contains(BindingFlag.DEFERRED);
    }
    
    /**
     * @return True if no more binding functions should process the resulting
     *         desire
     */
    public boolean terminates() {
        return flags.contains(BindingFlag.TERMINAL);
    }

    public static class Builder {
        private Desire desire;
        private CachePolicy policy;
        private EnumSet<BindingFlag> flags = BindingFlag.emptySet();

        private Builder() {}

        public Builder setDesire(Desire desire) {
            this.desire = desire;
            return this;
        }

        public Builder setCachePolicy(CachePolicy policy) {
            this.policy = policy;
            return this;
        }

        public Builder addFlag(BindingFlag flag) {
            flags.add(flag);
            return this;
        }

        public BindingResult build() {
            com.google.common.base.Preconditions.checkState(desire != null, "no desire set");
            com.google.common.base.Preconditions.checkState(policy != null, "no policy set");
            return new BindingResult(desire, policy, flags);
        }
    }
}
