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

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Satisfaction;

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
     * Resolve all dependencies of <tt>rootSatisfaction</tt> given the set of
     * BindRules to apply. The BindRules are stored in a Map where the
     * ContextChain keys represent the context rules that activate that
     * collection of BindRules. The BindRules within a given collection for a
     * ContextChain need not apply to the same Desires or types, they are merely
     * activated at the same time.
     * <p>
     * This will return a ResolverResult containing a fully resolved dependency
     * graph for the root satisfaction.
     * 
     * @param rootSatisfaction The root object to be resolved
     * @param bindRules The bind rule configuration that specifies how to
     *            resolve desires into nodes
     * @return A completed dependency graph for the given root and rules
     * @throws ResolverException if the satisfaction could not be fully resolved
     * @throws NullPointerException if rootSatisfaction or bindRules are null
     */
    ResolverResult resolve(Satisfaction rootSatisfaction, Map<ContextChain, Collection<? extends BindRule>> bindRules);
}
