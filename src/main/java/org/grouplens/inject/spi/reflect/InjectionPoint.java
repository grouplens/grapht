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
package org.grouplens.inject.spi.reflect;

import org.grouplens.inject.spi.Desire;

/**
 * InjectionPoint represents a point of injection for an instantiable type.
 * Examples include a constructor parameter, a setter method, or a field.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface InjectionPoint {
    /**
     * Return the type required to satisfy the injection point.
     * 
     * @return The type of the injection point
     */
    Class<?> getType();

    /**
     * Return any role on this injection point, or null if it is the default
     * role.
     * 
     * @return The role on the injection point
     */
    AnnotationRole getRole();

    /**
     * Return whether or not this injection point is a transient, with the same
     * definition of {@link Desire#isTransient()}.
     * 
     * @return True if the injection point is for a transient desire
     */
    boolean isTransient();
}
