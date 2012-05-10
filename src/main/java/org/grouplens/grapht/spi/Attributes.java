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
package org.grouplens.grapht.spi;

import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.inject.Qualifier;

import org.grouplens.grapht.annotation.Attribute;

/**
 * Attributes contain additional annotations and metadata associated with an
 * injection point. This includes any {@link Qualifier} annotation that is
 * applied. Additional attributes can be defined by creating a new annotation
 * type that is itself annotated with {@link Attribute}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Attributes {
    /**
     * Return the qualifier annotation added to the injection point. The
     * returned annotation's type will have been annotated with
     * {@link Qualifier}. If the injection point is not qualified, this will
     * return null.
     * 
     * @return Any qualifier applied to the injection point
     */
    @Nullable Annotation getQualifier();
    
    /**
     * Return the attribute of type A that is applied to the injection point. If
     * the injection point does not have an attribute of A, then null is
     * returned.
     * 
     * @param atype Attribute annotation type
     * @return The instance of A applied to the injection point, or null
     * @throws NullPointerException if atype is null
     */
    @Nullable <A extends Annotation> A getAttribute(Class<A> atype);
    
    /**
     * @return Immutable collection of attribute annotations (does not include
     *         the qualifier)
     */
    Collection<Annotation> getAttributes();
}
