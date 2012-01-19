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

/**
 * CachePolicy controls the behavior of instant creation after dependency
 * resolution has been completed.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public enum CachePolicy {
    /**
     * A new instance is created every time a binding is used to satisfy a
     * dependency.
     */
    NEW,
    /**
     * <p>
     * Instances of a type are shared as much as possible. Because of
     * context-specific bindings, a type satisfying one dependency might require
     * a different set of resolved dependencies compared to another dependency
     * of the same type. In this case, separate instances are required.
     * <p>
     * However, when a type's resolved dependencies are the same, instances can
     * be shared. Thus, a type with no dependencies and a SHARED policy is
     * effectively a singleton within the scope of the configuration injector.
     */
    SHARED
}
