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
package org.grouplens.grapht;

import com.google.common.base.Predicate;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.solver.DesireChain;

import org.jetbrains.annotations.Nullable;
import java.io.Serializable;
import java.util.EnumSet;

/**
 * Track information about a particular resolved dependency. Used as the edge of DI graph nodes
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class Dependency implements Serializable {
    private static final long serialVersionUID = 1L;

    private final DesireChain desireChain;
    private final EnumSet<Flag> flags;

    private Dependency(DesireChain chain, EnumSet<Flag> flagSet) {
        desireChain = chain;
        flags = flagSet.clone();
    }

    public static Dependency create(DesireChain desires, EnumSet<Flag> flags) {
        return new Dependency(desires, flags);
    }

    /**
     * Get the desire chain associated with this dependency.
     * @return The chain of desires followed in resolving this dependency.
     */
    public DesireChain getDesireChain() {
        return desireChain;
    }

    /**
     * Convenience method to get the initial desire that prompted this dependency.
     * @return The initial desire that prompted this dependency.
     */
    public Desire getInitialDesire() {
        return desireChain.getInitialDesire();
    }

    /**
     * Get the flags associated with this dependency.
     * @return The flags associated with this dependency.
     */
    public EnumSet<Flag> getFlags() {
        return flags;
    }

    /**
     * Query whether this dependency is immune to rewriting.
     * @return {@code true} if this dependency cannot be rewritten during a graph rewrite.
     */
    public boolean isFixed() {
        return flags.contains(Flag.FIXED);
    }

    /**
     * Query whether this dependency has a particular initial desire.
     * @param d The desire.
     * @return {@code true} if the dependency's initial desire is {@code d}.
     */
    public boolean hasInitialDesire(Desire d) {
        return getInitialDesire().equals(d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dependency that = (Dependency) o;

        if (!desireChain.equals(that.desireChain)) return false;
        if (!flags.equals(that.flags)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = desireChain.hashCode();
        result = 31 * result + flags.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Dependency(" + desireChain + ", " + flags + ")";
    }

    /**
     * Flags associated with a dependency.
     */
    public static enum Flag {
        /**
         * Indicates that a dependency is immune to rewriting.
         */
        FIXED;

        public static EnumSet<Flag> emptySet() {
            return EnumSet.noneOf(Flag.class);
        }
    }
}
