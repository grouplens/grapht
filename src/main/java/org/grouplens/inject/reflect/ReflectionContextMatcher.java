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
package org.grouplens.inject.reflect;

import java.lang.annotation.Annotation;

import org.grouplens.inject.resolver.ContextMatcher;
import org.grouplens.inject.spi.SatisfactionAndRole;

public class ReflectionContextMatcher implements ContextMatcher {
    private final Class<?> type;
    private final Class<? extends Annotation> roleAnnotation;
    
    public ReflectionContextMatcher(Class<?> type) {
        this(type, null);
    }
    
    public ReflectionContextMatcher(Class<?> type, Class<? extends Annotation> role) {
        this.type = type;
        roleAnnotation = role;
    }
    
    @Override
    public boolean matches(SatisfactionAndRole n) {
        // FIXME: handle role inheritence, and generics correctly
        return type.isAssignableFrom(n.getSatisfaction().getErasedType()) && roleAnnotation.equals(null);
    }
}
