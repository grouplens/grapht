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
package org.grouplens.grapht;

/**
 * Interface for instantiating components.  It functions much like {@link javax.inject.Provider},
 * except that it reports failure with a checked exception.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.9
 */
public interface Instantiator{
    Object instantiate() throws ConstructionException;

    /**
     * Get the type that this instantiator will instantiate.
     * @return The type returned by this instantiator.
     */
    Class getType();
}
