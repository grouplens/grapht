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

    /**
     * Query whether this binding result should be skipped when one of its dependencies fails.
     * @return {@code true} if this binding result should be skipped if one of its dependencies fails.
     */
    public boolean isSkippable() {
        return flags.contains(BindingFlag.SKIPPABLE);
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

        public Builder setFlags(EnumSet<BindingFlag> flags) {
            this.flags = flags.clone();
            return this;
        }

        public BindingResult build() {
            com.google.common.base.Preconditions.checkState(desire != null, "no desire set");
            com.google.common.base.Preconditions.checkState(policy != null, "no policy set");
            return new BindingResult(desire, policy, flags);
        }
    }
}
