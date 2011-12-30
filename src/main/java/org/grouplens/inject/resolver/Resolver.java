/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.resolver;

import java.util.Collection;
import java.util.Map;

import org.grouplens.inject.graph.BindRule;
import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Graph;

/**
 * Resolvers are algorithms that resolve the dependencies of a set of
 * {@link Desire Desires} given a set of {@link BindRule BindRules} to apply.
 * These BindRules may be activated only when a specific dependency context is
 * present. The context can be thought of as all previous Nodes and Roles in the
 * dependency hierarchy that led to the desire being actively resolved.
 *
 * @see DefaultResolver
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Resolver {
    /**
     * <p>
     * Resolve all Desires within <tt>rootDesires</tt> given the set of
     * BindRules to apply. The BindRules are stored in a Map where the
     * ContextChain keys represent the context rules that activate that
     * collection of BindRules. The BindRules within a given collection for a
     * ContextChain need not apply to the same Desires or types, they are merely
     * activated at the same time.
     * <p>
     * This will return a graph containing the resolved Nodes for the root
     * desires, and all necessary edges and nodes to satisfy the root Nodes'
     * dependencies. If the desires or dependencies could not be satisfied,
     * either because there was not sufficient binding information or if there
     * were too many choices, then an exception is thrown.
     * 
     * @param rootDesires The root desires that must be resolved
     * @param bindRules The bind rule configuration that specifies how to
     *            resolve desires into nodes
     * @return A completed dependency graph for the given desires and rules
     * @throws ResolverException if the desires could not be fully resolved
     * @throws NullPointerException if rootDesires or bindRules are null
     */
    // FIXME: this can't just return a Graph because we have no easy way of
    // identifying the set of root nodes that the root desires mapped to.  We
    // need to return a Map<Desire, Node> as well as the graph to provide entry
    // points into the dependency graph
    Graph resolve(Collection<Desire> rootDesires, Map<ContextChain, Collection<BindRule>> bindRules);
}
