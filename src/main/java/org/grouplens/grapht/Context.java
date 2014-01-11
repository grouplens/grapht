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
package org.grouplens.grapht;

import javax.annotation.Nullable;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;

/**
 * <p>
 * Context is the main entry point for configuring bind rules using the fluent
 * API. The dependency injector uses the contexts to limit the scope of a
 * binding. Every time a dependency is satisfied, that type (and possibly {@link Qualifier})
 * is pushed onto the context stack. Thus, if two different types each require a
 * Foo, there can be two different bindings activated depending on which first
 * type is in the context stack.
 * <p>
 * The "root" context is an empty stack, and will always be matched. When
 * creating bindings, the context stack can be configured by calling
 * {@link #in(Class)} or {@link #in(Class, Class)}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface Context {
    /**
     * Start a new binding for the given type T within the scope of this
     * context.  The returned Binding instance can be configured and completed by
     * invoking one of its various to() methods.  Unless further configuration is
     * done, this binding will match unqualified dependencies and dependencies
     * with a qualifier annotated with
     * {@link org.grouplens.grapht.annotation.AllowUnqualifiedMatch}.
     * 
     * @param <T> The matched source type
     * @param type The raw class that is matched
     * @return A new binding in this context for type T
     */
    <T> Binding<T> bind(Class<T> type);

    /**
     * Start a new binding for a qualified type. A shortcut for
     * {@code bind(type).withQualifier(qual)}.
     * @param qual The type's qualifier.
     * @param type The type to bind.
     * @param <T> The type to bind.
     * @return A new binding in this context for T with qualifier qual.
     * @see Binding#withQualifier(Class)
     */
    <T> Binding<T> bind(Class<? extends Annotation> qual, Class<T> type);

    /**
     * Start a new binding for a type irrespective of qualifier.  This is a
     * shortcut for {@code bind(type).withAnyQualifier()}.
     * @param type The type.
     * @return A new binding in this context for type T with any (or no) 
     *         qualifier.
     */
    <T> Binding<T> bindAny(Class<T> type);

    /**
     * @deprecated Use {@link #within(Class)}.
     */
    @Deprecated
    Context in(Class<?> type);

    /**
     * @deprecated Use {@link #within(Class, Class)}.
     */
    @Deprecated
    Context in(@Nullable Class<? extends Annotation> qualifier, Class<?> type);

    /**
     * @deprecated Use {@link #within(Annotation, Class)}.
     */
    @Deprecated
    Context in(@Nullable Annotation qualifier, Class<?> type);

    /**
     * Create a new Context that extends the current context stack with the
     * given class type. This matches with the default {@link Qualifier}. This is equivalent
     * to <code>within(null, type);</code>
     *
     * @param type The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context within(Class<?> type);

    /**
     * Create a new Context that extends the current context stack with the
     * given class and {@link Qualifier} annotation. If the qualifier is null,
     * the default or null qualifier is used.
     *
     * @param qualifier The qualifier type that must be matched along with the type
     * @param type The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context within(@Nullable Class<? extends Annotation> qualifier, Class<?> type);

    /**
     * Create a new Context that extends the current context stack with the
     * given class, qualified by the specific Annotation instance. If the
     * qualifier is null, the default or null qualifier is used.
     *
     * <p>The annotation provided must be serializable.  Annotations built by {@link
     * org.grouplens.grapht.annotation.AnnotationBuilder} (recommended) or retrieved from the Java
     * reflection API are serializable; if you use some other annotation implementation, it must be
     * serializable.
     *
     * @param qualifier The qualifier instance that must be matched along with
     *            the type
     * @param type The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context within(@Nullable Annotation qualifier, Class<?> type);

    /**
     * Create a new Context that extends the current context stack with the given class type as an
     * anchored match. This matches with the default {@link Qualifier}. This is equivalent to
     * <code>at(null, type);</code>
     *
     * @param type The type to extend this context by
     * @return A new Context with a longer context stack
     * @see #at(Class, Class)
     */
    Context at(Class<?> type);

    /**
     * Create a new Context that extends the current context stack with the given class and {@link
     * Qualifier} annotation as an anchored match. If the qualifier is null, the default or null
     * qualifier is used.
     * <p>
     * Unlike {@link #in(Class,Class)}, this match is <em>anchored</em> &mdash; that is, it only
     * matches at the end of a context chain.  Context is matched if it ends with this or,
     * if further context is opened inside this context, if the inner context matches immediately.
     * </p>
     *
     * @param qualifier The qualifier type that must be matched along with the type
     * @param type      The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context at(@Nullable Class<? extends Annotation> qualifier, Class<?> type);

    /**
     * Create a new Context that extends the current context stack with the given class, qualified
     * by the specific Annotation instance. as an anchored match. If the qualifier is null, the
     * default or null qualifier is used.
     *
     * <p>The annotation provided must be serializable.  Annotations built by {@link
     * org.grouplens.grapht.annotation.AnnotationBuilder} (recommended) or retrieved from the Java
     * reflection API are serializable; if you use some other annotation implementation, it must be
     * serializable.
     *
     * @param qualifier The qualifier instance that must be matched along with the type
     * @param type      The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context at(@Nullable Annotation qualifier, Class<?> type);
}
