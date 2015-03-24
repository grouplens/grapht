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
package org.grouplens.grapht.context;

import org.grouplens.grapht.solver.InjectionContext;

/**
 * Interface for context matchers.  A context matcher matches contexts to
 * determine whether particular bind rules are to be applied.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface ContextMatcher {
    /**
     * Attempt to match this context matcher against the specified context. If the
     * matcher matches, it will return a {@link ContextMatch} describing the result.
     *
     * @param context The context to match.
     * @return The match information, or {@code null} if the matcher does not match.
     */
    ContextMatch matches(InjectionContext context);
}
