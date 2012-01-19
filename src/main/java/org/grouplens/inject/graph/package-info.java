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
/**
 * Interface to the type graph API used by the configuration injector. This API
 * provides means to compare and resolve types, get their dependencies, and
 * otherwise navigate the classpath in order to resolve dependencies. Types are
 * represented as a graph of nodes with edges to their dependencies.
 * 
 * <p>
 * Important to the graph API is the notion of a <i>concrete type</i>. Concrete
 * types are defined inductively:
 * <ul>
 * <li>Any non-parameterized, non-abstract class is a concrete type.
 * <li>Any parameterized non-abstract class all of whose parameters are
 * bound to concrete types is a concrete type.
 * </ul>
 */
package org.grouplens.inject.graph;
