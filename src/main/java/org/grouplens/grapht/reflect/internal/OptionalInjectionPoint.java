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
package org.grouplens.grapht.reflect.internal;

import com.google.common.base.Preconditions;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.grouplens.grapht.reflect.InjectionPoint;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.annotation.Annotation;
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
