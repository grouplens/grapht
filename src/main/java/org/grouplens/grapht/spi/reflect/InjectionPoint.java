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
package org.grouplens.grapht.spi.reflect;

import java.lang.reflect.Member;

import org.grouplens.grapht.spi.Desire;

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
     * Return any Qualifier on this injection point, or null if it is the default
     * qualifier.
     * 
     * @return The qualifier on the injection point
     */
    AnnotationQualifier getQualifier();
    
    /**
     * @return The Member that produces this injection point
     */
    Member getMember();

    /**
     * Return whether or not this injection point is a transient, with the same
     * definition of {@link Desire#isTransient()}.
     * 
     * @return True if the injection point is for a transient desire
     */
    boolean isTransient();
    
    /**
     * @return True if this injection point accepts null values
     */
    boolean isNullable();
}
