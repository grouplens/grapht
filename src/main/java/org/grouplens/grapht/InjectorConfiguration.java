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
package org.grouplens.grapht;

import java.util.Collection;
import java.util.Map;

import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.InjectSPI;

/**
 * InjectorConfiguration is a simple container for the accumulation of
 * {@link BindRule BindRules}. Generally, the {@link InjectorBuilder} or
 * {@link InjectorConfigurationBuilder} can be used without any other
 * modification.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface InjectorConfiguration {
    /**
     * Get all bind rules in this configuration. The bind rules are organized
     * first by the context that they were declared in. Each possible context
     * can have multiple bind rules within it. These bind rules can be of any
     * type, they are not restricted to being of the same type or qualifier.
     * 
     * @return All bind rules, in an unmodifiable map
     */
    public Map<ContextChain, Collection<? extends BindRule>> getBindRules();
    
    /**
     * @return The InjectSPI used by this configuration
     */
    public InjectSPI getSPI();
}