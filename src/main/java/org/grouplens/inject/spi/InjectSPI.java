/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.spi;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.inject.Provider;

import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

/**
 * InjectSPI is a service provider interface for accessing and creating the
 * types needed to use graph-based injections. InjectSPIs are responsible for
 * creating concrete instances of {@link BindRule BindRules},
 * {@link ContextMatcher ContextMatchers}, and {@link Desire Desires}. These
 * created instances will also likely create SPI-specific implementations of
 * {@link Satisfaction} and {@link Qualifier}.
 * <p>
 * The {@link ReflectionInjectSPI} provides a complete implementation of the SPI
 * using reflection to analyze types.
 * 
 * @see ReflectionInjectSPI
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface InjectSPI {
    /**
     * Create a BindRule that matches the Qualifier and source pair, and binds
     * to a target class type. The weight is a sorting weight used to break up
     * ties between rules that match equally. See {@link BindRule} for more
     * details.
     * 
     * @param <T> The matched type
     * @param qualifier An optional qualifier any injection point must match
     * @param source The type any injection point must match
     * @param impl The implementation to satisfy the source
     * @param weight The sorting weight of the bind rule
     * @param terminate True if no other bind rules should be followed after
     *            this is matched
     * @return The bind rule binding qualifier:source to impl
     */
    <T> BindRule bindType(@Nullable Qualifier qualifier, Class<T> source,
                          Class<? extends T> impl, int weight, boolean terminate);

    /**
     * Create a BindRule that matches the Qualifier and source pair, and binds
     * to an instance of the source type. See {@link BindRule} for more details
     * about the weight parameter. The created bind rule should return false
     * from {@link BindRule#terminatesChain()}.
     * 
     * @param <T> The matched type
     * @param qualifier An optional Qualifier any injection point must match
     * @param source The type any injection point must match
     * @param instance The instance used to satisfy injection points
     * @param weight The sorting weight for the bind rule
     * @return The bind rule binding qualifier:source to instance
     */
    <T> BindRule bindInstance(@Nullable Qualifier qualifier, Class<T> source,
                              T instance, int weight);

    /**
     * Create a BindRule that matches the Qualifier and
     * source pair, and binds to a Provider class type. See {@link BindRule} for
     * more details about the weight parameter. The created bind rule should
     * return false from {@link BindRule#terminatesChain()}.
     * 
     * @param <T> The matched type
     * @param qualifier An optional qualifier any injection point must
     *            match
     * @param source The type any injection point must match
     * @param providerType The provider type that can create instances used to
     *            satisfy injection points
     * @param weight The sorting weight for the bind rule
     * @return The bind rule binding qualifier:source to providerType
     */
    <T> BindRule bindProvider(@Nullable Qualifier qualifier, Class<T> source, 
                              Class<? extends Provider<? extends T>> providerType, int weight);

    /**
     * Create a BindRule that matches the Qualifier and
     * source pair, and binds to a Provider instance. See {@link BindRule} for
     * more details about the weight parameter. The created bind rule should
     * return false from {@link BindRule#terminatesChain()}.
     * 
     * @param <T> The matched type
     * @param qualifier An optional qualifier any injection point must
     *            match
     * @param source The type any injection point must match
     * @param provider The provider that can create instances used to satisfy
     *            injection points
     * @param weight The sorting weight for the bind rule
     * @return The bind rule binding qualifier:source to provider
     */
    <T> BindRule bindProvider(@Nullable Qualifier qualifier, Class<T> source, 
                              Provider<? extends T> provider, int weight);

    /**
     * Create a ContextMatcher that matches the given context formed by a
     * Qualifier and a type. If the qualifier is null, it is
     * the default qualifier. The created ContextMatcher must be
     * compatible with the BindRules, Desires, and Satisfactions created by this
     * InjectSPI.
     * 
     * @param qualifier The optional qualifier
     * @param type The type of the context
     * @return A ContextMatcher representing the qualifier and type
     */
    ContextMatcher context(@Nullable Qualifier qualifier, Class<?> type);

    /**
     * Create a Desire that wraps the Qualifier and type. If the qualifier is
     * null, the default qualifier is used. The created Desire must be
     * compatible with the BindRules, ContextMatchers, and Satisfactions created
     * by this InjectSPI.
     * 
     * @param qualifier The optional qualifier
     * @param type The desired type
     * @param nullable Whether or not the desire can be nullable
     * @return A Desire wrapping the qualifier and type
     */
    Desire desire(@Nullable Qualifier qualifier, Class<?> type, boolean nullable);
    
    /**
     * Create a Qualifier that wraps the given annotation. This annotation must
     * be annotated with {@link javax.inject.Qualifier}. This should return null
     * if the annotation is null.
     * 
     * @param qualifier The qualifier annotation
     * @return A Qualifier wrapping the annotation
     */
    Qualifier qualifier(@Nullable Class<? extends Annotation> qualifier);
    
    /**
     * Create a name-based qualifier. This can be used to support the
     * {@link Named} annotation because {@link #qualifier(Class)} is not
     * sufficient to capture the String values associated with the injection
     * points. This should return null if the name is null.
     * 
     * @param name The name to match
     * @return A Qualifier wrapping the string name
     */
    Qualifier qualifier(@Nullable String name);
}
