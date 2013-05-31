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

import org.grouplens.grapht.solver.BindRule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Qualifier;
import java.lang.annotation.Annotation;

/**
 * Binding is part of the fluent API used for configuring an {@link Injector}.
 * It represents a binding action from one type to another type.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The source type
 */
public interface Binding<T> {
    /**
     * <p>
     * Configure the binding to match the given {@link Qualifier} annotation.
     * The given annotation type must be annotated with {@link Qualifier}. The
     * created binding will match injection points only if the qualifier is
     * applied to the injection point, unless the annotation inherits from the
     * default qualifier.
     * <p>
     * This will override any previous name or qualifier annotation.
     * 
     * @param qualifier The Qualifier that must match
     * @return A newly configured Binding
     */
    Binding<T> withQualifier(@Nonnull Class<? extends Annotation> qualifier);

    /**
     * Configure the binding to match injection points that have been annotated with the exact
     * annotation instance.
     *
     * <p>This will override any previous name or qualifier annotation.
     *
     * <p>The annotation provided must be serializable.  Annotations built by {@link
     * org.grouplens.grapht.annotation.AnnotationBuilder} (recommended) or retrieved from the Java
     * reflection API are serializable; if you use some other annotation implementation, it must be
     * serializable.
     *
     * @param annot The annotation instance to match.
     * @return A newly configured Binding
     */
    Binding<T> withQualifier(@Nonnull Annotation annot);

    /**
     * Configure the binding to match injection points that have any qualifier annotation (including
     * no qualifier).
     *
     * <p>This will override any previous name or qualifier annotation.
     *
     * @return A newly configured Binding
     */
    Binding<T> withAnyQualifier();

    /**
     * <p>
     * Configure the binding to only match injection points that have no
     * qualifier. By default, the binding matches any injection point with the
     * given type, whether or not its been qualified. A qualified binding to the
     * same type will still be preferred first when resolving a qualified
     * injection point.
     * 
     * @return A newly configured binding
     */
    Binding<T> unqualified();

    /**
     * Exclude the provided type from being matched when examining injection
     * points. Bindings can generate multiple {@link BindRule BindRules} for
     * super and sub types. Excluded classes for a binding will not have
     * BindRules generated for them.
     * 
     * @param exclude The type to exclude from automated rule generation
     * @return A newly configured Binding
     */
    Binding<T> exclude(@Nonnull Class<?> exclude);
    
    /**
     * Configure the binding so that a shared instance is always used when
     * satisfying matched injection points, effectively making it a singleton or
     * memoized within its container.
     * 
     * @return A newly configured Binding
     */
    Binding<T> shared();
    
    /**
     * Configure the binding so that new instances are always created when
     * satisfying matched injection.
     * 
     * @return A newly configured binding
     */
    Binding<T> unshared();

    /**
     * <p>
     * Complete this binding by specifying a subtype that will satisfy the
     * desired type. The implementation does not have to be instantiable; if
     * it's not then additional bindings must be configured to bind to reach an
     * instantiable type. It is recommended for types to be instantiable.
     * <p>
     * The given type may have its own dependencies that will have to be
     * satisfied by other bindings.
     * <p>It is permissible to have two bindings forming a chain, like
     * <code>A &rarr; B &rarr; C</code>. The {@code chained} parameter controls
     * whether the chain is followed.  If {@code chained == false} for the {@code A &rarr; B}
     * binding, then the {@code B &rarr; C} binding is not followed (and the {@code A &rarr; B}
     * binding is called <emph>terminal</emph>).
     * 
     * @param impl The implementation type
     * @param chained Whether further binding lookup will be done on the implementation type.
     *                {@code true} allows lookup, {@code false} creates a terminal binding.
     */
    void to(@Nonnull Class<? extends T> impl, boolean chained);

    /**
     * Bind to an implementation type non-terminally.  This calls {@link #to(Class, boolean)}
     * as {@code this.to(impl, true)}.
     *
     * @param impl The implementation type.
     */
    void to(@Nonnull Class<? extends T> impl);

    /**
     * Complete this binding by specifying an instance to use. The instance will
     * be used to satisfy matched injection points. Because the instance never
     * changes, any cache policy assigned by {@link #shared()} or
     * {@link #unshared()} is effectively ignored.
     * 
     * @param instance The instance to use. If {@code null}, binds explicitly to
     *                 null.
     */
    void to(@Nullable T instance);

    /**
     * Complete this binding by specifying a Provider class to be instantiated
     * and used to create instances of type T. The Provider class may have its
     * own dependencies that will be resolved by the injector.
     * 
     * @param provider The provider type that will satisfy this binding
     */
    void toProvider(@Nonnull Class<? extends Provider<? extends T>> provider);

    /**
     * Complete this binding by specifying a Provider instance that will be used
     * to create instances of type T to satisfy this binding.
     * 
     * @param provider The provider instance
     */
    void toProvider(@Nonnull Provider<? extends T> provider);


    /**
     * Complete this binding by explicitly binding to {@code null}. The resulting
     * bindings may not create an instantiable graph, as non-nullable injection points
     * still require a non-null instance.
     */
    void toNull();

    /**
     * Complete this binding by explicitly binding to {@code null} with a type.
     * @param type The type of {@code null} to bind.
     * @see #toNull()
     */
    void toNull(Class<? extends T> type);
}
