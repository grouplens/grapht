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
package org.grouplens.inject;

import java.util.Collection;
import java.util.Map;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.resolver.Resolver;
import org.grouplens.inject.spi.BindRule;

/**
 * InjectorConfiguration is a simple container for the accumulation of
 * {@link BindRule BindRules}. Generally, the {@link InjectorBuilder} or
 * {@link InjectorConfigurationBuilder} can be used without any other
 * modification. {@link Resolver Resolvers} may depend on an
 * InjectorConfiguration so they know what types are bound to which
 * dependencies.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface InjectorConfiguration {
    /**
     * Get all bind rules in this configuration. The bind rules are organized
     * first by the context that they were declared in. Each possible context
     * can have multiple bind rules within it. These bind rules can be of any
     * type, they are not restricted to being of the same type or role.
     * 
     * @return All bind rules, in an unmodifiable map
     */
    public Map<ContextChain, Collection<? extends BindRule>> getBindRules();
}
