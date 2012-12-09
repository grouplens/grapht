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
package org.grouplens.grapht.spi;

import java.io.Serializable;

import javax.inject.Qualifier;

import org.apache.commons.lang3.tuple.Pair;

/**
 * <p>
 * ContextElementMatcher represents a "pattern" that can match an element within the
 * dependency context created as a Resolver follows a dependency hierarchy. The
 * dependency context is an ordered list of Nodes and the Roles of the Desires
 * that the Nodes satisfy. The top-level Node can be thought of as the root Node
 * or first element within the dependency context. Later elements represent
 * dependencies or dependencies of dependencies of the root node.
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
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface ContextElementMatcher extends Serializable {
    /**
     * Return true if this ContextElementMatcher matches or applies to the given
     * Satisfaction and Qualifier.
     * 
     * @param n The node and attributes in the current dependency context
     * @return True if this matcher matches the node and attributes, false
     *         otherwise
     */
    boolean matches(Pair<Satisfaction, Attributes> n);

    /**
     * Query whether this element matcher is anchored. Anchored matchers must
     * match immediately (scanning from the end of the context); unanchored
     * matchers can be matched at first opportunity.
     *
     * @return {@code true} if this context matcher is anchored.
     */
    boolean isAnchored();
}
