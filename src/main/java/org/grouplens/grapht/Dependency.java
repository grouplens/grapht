/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
