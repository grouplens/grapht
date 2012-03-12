/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;

import org.grouplens.inject.annotation.Parameter;
import org.grouplens.inject.annotation.Role;
import org.grouplens.inject.resolver.ContextChain;

/**
 * <p>
 * Context is the main entry point for configuring bind rules using the fluent
 * API. The dependency injector uses the contexts to limit the scope of a
 * binding. Every time a dependency is satisfied, that type (and possibly role)
 * is pushed onto the context stack. Thus, if two different types each require a
 * Foo, there can be two different bindings activated depending on which first
 * type is in the context stack.
 * <p>
 * The "root" context is an empty stack, and will always be matched. When
 * creating bindings, the context stack can be configured by calling
 * {@link #in(Class)} or {@link #in(Class, Class)}.
 * 
 * @see ContextChain
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Context {
    /**
     * Start a new binding for the given type T within the scope of this
     * context. The returned Binding instance can be configured and completed by
     * invoking one of its various to() methods.
     * 
     * @param <T> The matched source type
     * @param type The raw class that is matched
     * @return A new binding in this context for type T
     */
    <T> Binding<T> bind(Class<T> type);

    /**
     * <p>
     * Bind a parameter value. This is a convenience for creating a Binding
     * using the proper primitive class or String of the parameter, specifying
     * the given annotation as its role, and binding it to the given instance.
     * The role annotation provided should be annotated with {@link Parameter}
     * instead of {@link Role}.
     * <p>
     * As an example:
     * 
     * <pre>
     * context.bind(IntFoo.class, 5);
     * </pre>
     * 
     * is equivalent to:
     * 
     * <pre>
     * context.bind(Integer.class).withRole(IntFoo.class).to(5);
     * </pre>
     * 
     * @see Parameter
     * @param param The parameter annotation
     * @param value The instance that is bound
     */
    void bind(Class<? extends Annotation> param, Object value);

    /**
     * Create a new Context that extends the current context stack with the
     * given class type. This matches with the default role. This is equivalent
     * to <code>in(null, type);</code>
     * 
     * @param type The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context in(Class<?> type);
    
    /**
     * Create a new Context that extends the current context stack with the
     * given class and role. If the role is null, the default role is used.
     * @param role The role that must be matched along with the type
     * @param type The type to extend this context by
     * @return A new Context with a longer context stack
     */
    Context in(@Nullable Class<? extends Annotation> role, Class<?> type);
}
