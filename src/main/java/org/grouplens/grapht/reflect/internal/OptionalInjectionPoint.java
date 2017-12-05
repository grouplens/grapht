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
package org.grouplens.grapht.reflect.internal;

import com.google.common.base.Preconditions;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * An optional injection point that wraps values in {@link Optional}.
 */
@ThreadSafe
public class OptionalInjectionPoint implements InjectionPoint {
    private final InjectionPoint delegate;
    private transient Type type;

    public OptionalInjectionPoint(InjectionPoint base) {
        Preconditions.checkArgument(base.getErasedType().equals(Optional.class),
                                    "delegate must have type Optional");
        delegate = base;
        type = unwrapOptionalType(base.getType());
    }

    private Type unwrapOptionalType(Type base) {
        Map<TypeVariable<?>, Type> args = TypeUtils.getTypeArguments(base, Optional.class);
        TypeVariable<?> tv = Optional.class.getTypeParameters()[0];
        Type t = args.get(tv);
        Preconditions.checkArgument(t != null, "delegate type must have resolved T");
        return t;
    }

    @Override
    public Type getType() {
        Type tp = type;
        if (tp == null) {
            type = tp = unwrapOptionalType(delegate.getType());
        }
        return type;
    }

    @Override
    public Class<?> getErasedType() {
        return TypeUtils.getRawType(type, null);
    }

    @Nullable
    @Override
    public Annotation getQualifier() {
        return delegate.getQualifier();
    }

    @Nullable
    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return delegate.getAttribute(atype);
    }

    @NotNull
    @Override
    public Collection<Annotation> getAttributes() {
        return delegate.getAttributes();
    }

    @Nullable
    @Override
    public Member getMember() {
        return delegate.getMember();
    }

    @Nullable
    @Override
    public AnnotatedElement getElement() {
        return delegate.getElement();
    }

    @Override
    public Object transform(Object obj) {
        return Optional.ofNullable(obj);
    }

    @Override
    public boolean isOptional() {
        return true;
    }

    @Override
    public int getParameterIndex() {
        return delegate.getParameterIndex();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(1)
                .append(delegate.hashCode())
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof OptionalInjectionPoint) {
            return delegate.equals(((OptionalInjectionPoint) obj).delegate);
        } else {
            return false;
        }
    }
}
