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

import org.grouplens.grapht.annotation.AnnotationBuilder;

import javax.inject.Named;


/**
 * Names is a utility class to create {@link Named} annotation instances when
 * configuring an injector that relies on named qualifiers. Qualifiers with
 * other attributes can by instantiated using the {@link AnnotationBuilder}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class Names {
    /**
     * Get a Named annotation instance whose value equals the provided String.
     * The returned annotation is equal to annotations created by a declaration
     * matching: <code>@Named(name)</code>, where name is the input String.
     * 
     * @param name The name value for the returned annotation
     * @return A Named instance wrapping the given name
     */
    public static Named named(String name) {
        return new AnnotationBuilder<Named>(Named.class)
            .setValue(name)
            .build();
    }
}
