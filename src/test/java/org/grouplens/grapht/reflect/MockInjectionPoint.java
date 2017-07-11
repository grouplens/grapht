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
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.util.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

/**
 * MockInjectionPoint is a simple injection point that wraps a type, qualifier, and a
 * transient state. It has no actual injectable point but can be used when
 * constructing ReflectionDesires on the fly for tests.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MockInjectionPoint implements InjectionPoint {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private final boolean nullable;
    private final Annotation qualifier;
    
    public MockInjectionPoint(Class<?> type, boolean nullable) {
        this(type, null, nullable);
    }
    
    public MockInjectionPoint(Class<?> type, Annotation qualifier, boolean nullable) {
        this.type = Types.box(type);
        this.qualifier = qualifier;
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
    public Type getType() {
        return type;
    }
    
    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Nullable
    @Override
    public Annotation getQualifier() {
        return qualifier;
    }

    @Nullable
    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return null;
    }

    @Nonnull
    @Override
    public Collection<Annotation> getAttributes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isOptional() {
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
               (m.qualifier == qualifier || (qualifier != null && qualifier.equals(m.qualifier)));
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (qualifier == null ? 0 : qualifier.hashCode()) ^ (nullable ? 2 : 4);
    }
}
