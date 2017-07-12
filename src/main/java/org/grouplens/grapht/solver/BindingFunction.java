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

import org.grouplens.grapht.ResolutionException;

import org.jetbrains.annotations.Nullable;

/**
 * Locate bindings for an injection point.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface BindingFunction {
    /**
     * Find the applicable binding, if any, for a desire in a particular context.
     *
     * @param context The context.
     * @param desire The desire.
     * @return The result of binding {@code desire}, or {@code null} if there is no binding.
     * @throws ResolutionException If there is an error (such as ambiguous bindings).
     */
    @Nullable
    BindingResult bind(InjectionContext context, DesireChain desire) throws ResolutionException;
}
