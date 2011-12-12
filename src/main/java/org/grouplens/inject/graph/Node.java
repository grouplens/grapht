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
package org.grouplens.inject.graph;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Function;

/**
 * A concrete type. It has a set of dependencies which must be satisfied in
 * order to instantiate it. It can also be viewed as an instantiable extension
 * of {@link Type}.
 * <p>
 * Nodes are expected to provide a reasonable implementation of
 * {@link #equals(Object)} and {@link #hashCode()} so that they can be
 * de-duplicated, etc.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface Node {
    /**
     * Get this node's dependencies.
     *
     * @return A list of dependencies which must be satisfied in order to
     *         instantiate this node.
     */
    List<Desire> getDependencies();

    /**
     * Get the type of this node.
     *
     * @return The type of objects to be instantiated by this node.
     */
    Type getType();

    /**
     * Get the type-erased class of this node's type.
     *
     * @return The class object for this node's type.
     */
    Class<?> getErasedType();

    /**
     * Create a provider from this node.
     *
     * @param dependencies A function mapping desires to providers of their
     *                     instances.
     * @return A provider of new instances of the type specified by this node,
     *         instantiated using the specified dependency mapping.
     */
    Provider<?> makeProvider(Function<Desire, Provider<?>> dependencies);
}
