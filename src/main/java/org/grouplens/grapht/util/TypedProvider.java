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
package org.grouplens.grapht.util;

import javax.inject.Provider;

/**
 * A provider that can report at runtime the type it will provide.  This is mostly to allow instance
 * and wrapper providers to pass through type information.
 *
 * @since 0.6
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface TypedProvider<T> extends Provider<T> {
    /**
     * Get the type of object that will be provided by this provider.  Any object returned from
     * {@link #get()} must be of this type.  This is used to help the injector refine its types.
     *
     * @return The type of objects that will be provided.
     */
    Class<?> getProvidedType();
}
