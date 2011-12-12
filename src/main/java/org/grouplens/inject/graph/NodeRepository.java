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

/**
 * A repository for obtaining type nodes and resolving desires.  The reflection
 * implementation uses annotations and subclassing relationships to attempt to
 * resolve desires from the classpath.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface NodeRepository {
    /**
     * Look up the node for a desire, using whatever default lookup rules are
     * specified.
     * 
     * @param desire The desire to resolve.
     * @return The node resolved by this desire, or <tt>null</tt> if the desire
     *         is not instantiable and cannot be resolved.
     */
    Node resolve(Desire desire);
    
    /**
     * Get a bind rule which uses the repository's defaults to resolve desires.
     * For any desire, this bind rule will compare less than all other bind
     * rules applicable to that desire.
     * 
     * @return A bind rule that uses defaults and annotations to resolve
     *         desires.
     */
    BindRule defaultBindRule();
}
