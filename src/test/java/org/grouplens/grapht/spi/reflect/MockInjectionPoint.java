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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.util.Types;

/**
 * MockInjectionPoint is a simple injection point that wraps a type, qualifier, and a
 * transient state. It has no actual injectable point but can be used when
 * constructing ReflectionDesires on the fly for tests.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockInjectionPoint implements InjectionPoint {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private final Attributes attrs;
    private final boolean nullable;
    
    public MockInjectionPoint(Class<?> type, boolean nullable) {
        this(type, new Annotation[0], nullable);
    }
    
    public MockInjectionPoint(Class<?> type, Annotation qualifier, boolean nullable) {
        this(type, (qualifier == null ? new Annotation[0] : new Annotation[] { qualifier }), nullable);
    }
    
    public MockInjectionPoint(Class<?> type, Annotation[] annots, boolean nullable) {
        this.type = Types.box(type);
        this.attrs = new AttributesImpl(annots);
        this.nullable = nullable;
    }
    
    @Override
    public Member getMember() {
        return new Member() {
            @Override
            public Class<?> getDeclaringClass() {
                return Void.class;
            }

            @Override
            public String getName() {
                return "synthetic";
            }

            @Override
            public int getModifiers() {
                return 0;
            }

            @Override
            public boolean isSynthetic() {
                return true;
            }
        };
    }
    
    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Attributes getAttributes() {
        return attrs;
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
               m.nullable == nullable && 
               m.attrs.equals(attrs);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ attrs.hashCode() ^ (nullable ? 2 : 4);
    }
}
