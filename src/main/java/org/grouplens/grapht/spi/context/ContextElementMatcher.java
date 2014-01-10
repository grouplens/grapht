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
package org.grouplens.grapht.spi.context;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Satisfaction;

import javax.inject.Qualifier;
import java.io.Serializable;

/**
 * <p>
 * ContextElementMatcher represents a "pattern" that can match an element within the
 * dependency context created as a Resolver follows a dependency hierarchy. The
 * dependency context is an ordered list of satisfactions and the qualifiers of the desires they satisfy.
 * The first satisfaction is the root satisfaction, a {@code null} satisfaction of type {@code void}.
 * <p>
 * ContextMatchers can match or apply to these nodes and {@link Qualifier}s
 * within a dependency context. As an example, the reflection based
 * ContextElementMatcher matches nodes that are sub-types of the type the matcher was
 * configured with.
 * <p>
 * ContextMatchers are composed into a list with {@link ElementChainContextMatcher} to
 * parallel the composing of nodes and {@link Qualifier}s into the dependency
 * context list. The ElementChainContextMatcher can then be used to determine if the list of
 * ContextMatchers applies to any given dependency context.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface ContextElementMatcher extends Serializable {
    /**
     * Return true if this ContextElementMatcher matches or applies to the given Satisfaction and
     * Qualifier.
     *
     * @param n The node and attributes in the current dependency context
     * @param position The position, from the beginning (to help in ordering match elements).
     * @return A match if this matcher matches the provided node label, or {@code false} if there is
     *         no match.
     */
    MatchElement apply(Pair<Satisfaction, Attributes> n, int position);
}
