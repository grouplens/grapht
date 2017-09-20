/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
