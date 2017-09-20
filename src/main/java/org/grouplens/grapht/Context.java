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
package org.grouplens.grapht;

import org.grouplens.grapht.context.ContextPattern;

import org.jetbrains.annotations.Nullable;
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
     * {@link org.grouplens.grapht.annotation.AllowDefaultMatch}.
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
     * Restruct context to matching a particular pattern.  The pattern is appended to the pattern
     * generated by the other context-restriction methods.  This is a high-powered method that
     * won't be needed in most situations, but allows fine-grained control of context matching.
     *
     * @param pattern The context pattern to match.
     * @return A context with longer context.
     */
    Context matching(ContextPattern pattern);

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
