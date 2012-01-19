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
package org.grouplens.inject.spi.reflect;

import javax.annotation.Nullable;

/**
 * MockInjectionPoint is a simple injection point that wraps a type, role, and a
 * transient state. It has no actual injectable point but can be used when
 * constructing ReflectionDesires on the fly for tests.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockInjectionPoint implements InjectionPoint {
    private final Class<?> type;
    private final AnnotationRole role;
    private final boolean trans;
    
    public MockInjectionPoint(Class<?> type, @Nullable AnnotationRole role, boolean trans) {
        this.type = Types.box(type);
        this.role = role;
        this.trans = trans;
    }
    
    
    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }

    @Override
    public boolean isTransient() {
        return trans;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MockInjectionPoint)) {
            return false;
        }
        MockInjectionPoint m = (MockInjectionPoint) o;
        return m.type.equals(type) && (m.role == null ? role == null : m.role.equals(role)) && m.trans == trans;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (role == null ? 0 : role.hashCode()) ^ (trans ? 1 : 0);
    }
}
