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
package org.grouplens.grapht.solver;

import java.util.EnumSet;

/**
 * Flags controlling binding behavior.
 */
public enum BindingFlag {
    /**
     * The binding is fixed (its result cannot be rewritten in graph rewriting).
     */
    FIXED,
    /**
     * The binding's resulting desire should have subsequent resolution deferred.
     */
    DEFERRED,
    /**
     * The binding is terminal (no further rules should be applied).
     */
    TERMINAL;

    public static EnumSet<BindingFlag> emptySet() {
        return EnumSet.noneOf(BindingFlag.class);
    }

    public static EnumSet<BindingFlag> set(BindingFlag... flags) {
        EnumSet<BindingFlag> set = emptySet();
        for (BindingFlag flag: flags) {
            set.add(flag);
        }
        return set;
    }
}
