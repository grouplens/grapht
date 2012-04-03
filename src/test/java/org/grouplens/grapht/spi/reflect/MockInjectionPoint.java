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

import org.grouplens.grapht.spi.reflect.AnnotationQualifier;
import org.grouplens.grapht.spi.reflect.InjectionPoint;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nullable;

/**
 * MockInjectionPoint is a simple injection point that wraps a type, qualifier, and a
 * transient state. It has no actual injectable point but can be used when
 * constructing ReflectionDesires on the fly for tests.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockInjectionPoint implements InjectionPoint {
    private final Class<?> type;
    private final AnnotationQualifier qualifier;
    private final boolean trans;
    private final boolean nullable;
    
    public MockInjectionPoint(Class<?> type, @Nullable AnnotationQualifier qualifier, boolean trans, boolean nullable) {
        this.type = Types.box(type);
        this.qualifier = qualifier;
        this.trans = trans;
        this.nullable = nullable;
    }
    
    
    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public AnnotationQualifier getQualifier() {
        return qualifier;
    }

    @Override
    public boolean isTransient() {
        return trans;
    }
    
    @Override
    public boolean isNullable() {
        return nullable;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MockInjectionPoint)) {
            return false;
        }
        MockInjectionPoint m = (MockInjectionPoint) o;
        return m.type.equals(type) && 
               m.trans == trans &&
               m.nullable == nullable && 
               (m.qualifier == null ? qualifier == null : m.qualifier.equals(qualifier));
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (qualifier == null ? 0 : qualifier.hashCode()) ^ (trans ? 1 : 0) ^ (nullable ? 2 : 4);
    }
}
