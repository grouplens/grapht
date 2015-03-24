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
package org.grouplens.grapht.reflect;

import java.io.Serializable;

/**
 * A possibly-not-concrete type. This represents the type of a dependency; it
 * may or may not be concrete. It can effectively be any type. Desires are
 * iteratively resolved and narrowed until they finally correspond to
 * {@link Satisfaction}s.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 *
 */
public interface Desire extends Serializable {
    /**
     * Query whether this desire is instantiable, that is, resolved to a
     * concrete type. If it is instantiable, then it can be converted to a Satisfaction
     * with {@link #getSatisfaction()}.
     *
     * @return <tt>true</tt> if the desire is for a concrete class. The only
     *         further desires or satisfactions that can satisfy it are for subclasses
     *         of the desire type.
     */
    boolean isInstantiable();

    /**
     * Get the satisfaction (concrete type) if this desire is fully resolved.
     *
     * @return The satisfaction for this desire, or <tt>null</tt> if the desire is not a
     *         concrete type.
     */
    Satisfaction getSatisfaction();

    /**
     * @return The injection point of this desire
     */
    InjectionPoint getInjectionPoint();
    
    /**
     * @return The desired type, potentially more constrained than the injection
     *         point's type
     */
    Class<?> getDesiredType();
    
    /**
     * Return a new Desire that restricts the type of this desire to the given
     * class. The type must be a subclass of the desired type.
     * 
     * @param type The restricted type
     * @return A restricted Desire
     */
    Desire restrict(Class<?> type);
    
    /**
     * Return a new Desire that restricts the type of this desire to the erased
     * type of the satisfaction. The returned Desire will also be instantiable
     * and return the provided satisfaction from {@link #getSatisfaction()}.
     * 
     * @param satisfaction The satisfaction to restrict this desire to
     * @return A restricted and satisfied desire
     */
    Desire restrict(Satisfaction satisfaction);
}
