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
package org.grouplens.grapht.util;

/**
 * Function represents a mathematical function that operates on an input domain
 * and returns results in an output range. These can be of separate types.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <I> The type of the input
 * @param <O> The type of the output
 */
public interface Function<I, O> {
    /**
     * Apply this function to the input and return the computed output.
     * 
     * @param input The input arguments to the function
     * @return The output of running the function
     */
    O apply(I input);
}
