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
package org.grouplens.grapht.reflect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collection;


/**
 * InjectionPoint represents a point of injection for an instantiable type.
 * Examples include a constructor parameter, a setter method, or a field.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface InjectionPoint extends Serializable {
    /**
     * Return the type required to satisfy the injection point.
     * 
     * @return The type of the injection point
     */
    Type getType();
    
    /**
     * @return Return the erased type of {@link #getType()}
     */
    Class<?> getErasedType();

    /**
     * Return the qualifier annotation added to the injection point. The
     * returned annotation's type will have been annotated with
     * {@link javax.inject.Qualifier}. If the injection point is not qualified, this will
     * return null.
     *
     * @return Any qualifier applied to the injection point
     */
    @Nullable
    Annotation getQualifier();

    /**
     * Return the attribute of type A that is applied to the injection point. If
     * the injection point does not have an attribute of type {@code A}, then null is
     * returned.
     *
     * @param atype Attribute annotation type.  It must be annotated with {@link org.grouplens.grapht.annotation.Attribute}.
     * @return The instance of A applied to the injection point, or null
     * @throws NullPointerException if atype is null
     */
    @Nullable <A extends Annotation> A getAttribute(Class<A> atype);

    /**
     * @return Immutable collection of attribute annotations (does not include
     *         the qualifier)
     */
    @Nonnull
    Collection<Annotation> getAttributes();
    
    /**
     * Return the Member that produced this injection point. Synthetic injection
     * points can have a null member.
     * 
     * @return The Member that produces this injection point
     */
    @Nullable Member getMember();

    /**
     * @return True if this injection point accepts null values
     */
    boolean isNullable();
}
