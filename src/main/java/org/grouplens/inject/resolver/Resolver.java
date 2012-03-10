/*
 * LensKit, an open source recommender systems toolkit.
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

import java.util.List;

import javax.inject.Provider;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Role;
import org.grouplens.inject.spi.Satisfaction;

/**
 * Resolvers are algorithms that resolve the dependencies of a set of
 * {@link Desire Desires} given a set of {@link BindRule BindRules} to apply.
 * These BindRules may be activated only when a specific dependency context is
 * present. The context can be thought of as all previous Nodes and Roles in the
 * dependency hierarchy that led to the desire being actively resolved.
 *
 * @see DefaultResolver
 * @
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Resolver {
    /**
     * <p>
     * Return a Provider that creates instances satisfying the given
     * <tt>desire</tt>. The Resolver will use the BindRules previously
     * configured for this Resolver. When determining context matching, this
     * desire will is in the root context.
     * 
     * @param desire The desire to be resolved
     * @return A provider that creates instances satisfying the desire
     * @throws ResolverException if the desire could not be fully resolved
     * @throws NullPointerException if desire is null
     */
    Provider<?> resolve(Desire desire);

    /**
     * Resolve the given Desire to a provider, as if the desire were needed in
     * the given context. A null context is the same as the root context.
     * 
     * @review Is this necessary? If so, should it be at this API level?
     * @param desire The desire to be resolved
     * @param context The context this desire is resolved within
     * @return A provider creating instances satisfying the desire
     * @throws NullPointerException if desire is null
     */
    Provider<?> resolve(Desire desire, List<Pair<Satisfaction, Role>> context);
    
    /**
     * @return The current dependency graph state cached by this Resolver
     */
    Graph<Satisfaction, Desire> getGraph();
}
