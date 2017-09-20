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
    TERMINAL,
    /**
     * The binding should be skipped if one of its results' dependencies cannot be satisfied.
     */
    SKIPPABLE;

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
