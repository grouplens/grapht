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
package org.grouplens.inject;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.inject.resolver.DefaultResolver;
import org.grouplens.inject.resolver.Resolver;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

/**
 * <p>
 * InjectorBuilder is a Builder implementation that is capable of creating a
 * simple {@link Injector}. Additionally, it is root {@link Context} to make
 * configuring the built Injector as easy as possible. Injectors created by
 * InjectorBuilder instances memoize their created objects.
 * </p>
 * <p>
 * Internally, it uses an {@link InjectorConfigurationBuilder} to accumulate
 * bind rules, a {@link DefaultResolver} to resolve dependencies, and the
 * {@link ReflectionInjectSPI} to access dependency information.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class InjectorBuilder implements Context, Builder<Injector> {
    private final InjectorConfigurationBuilder builder;

    /**
     * Create a new InjectorBuilder that automatically applies the given Modules
     * via {@link #applyModule(Module)}. Additional Modules can be applied later
     * as well. Configuration via the {@link Context} interface is also possible
     * (and recommended if Modules aren't used) before calling {@link #build()}.
     * 
     * @param modules Any modules to apply immediately
     */
    public InjectorBuilder(Module... modules) {
        builder = new InjectorConfigurationBuilder();
        for (Module m: modules) {
            applyModule(m);
        }
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return builder.getRootContext().bind(type);
    }
    
    @Override
    public void bind(Class<? extends Annotation> param, Object value) {
        builder.getRootContext().bind(param, value);
    }

    @Override
    public Context in(Class<?> type) {
        return builder.getRootContext().in(type);
    }

    @Override
    public Context in(Class<? extends Annotation> qualifier, Class<?> type) {
        return builder.getRootContext().in(qualifier, type);
    }

    /**
     * Apply a module to the root context of this InjectorBuilder (i.e.
     * {@link Module#bind(Context)}).
     * 
     * @param module The module to apply
     * @return This InjectorBuilder
     */
    public InjectorBuilder applyModule(Module module) {
        builder.applyModule(module);
        return this;
    }

    @Override
    public Injector build() {
        Resolver resolver = new DefaultResolver(builder.build());
        return new SimpleInjector(builder.getSPI(), resolver);
    }
    
    private class SimpleInjector implements Injector {
        private final InjectSPI spi;
        private final Resolver resolver;
        
        public SimpleInjector(InjectSPI spi, Resolver resolver) {
            this.resolver = resolver;
            this.spi = spi;
        }
        
        public <T> T getInstance(Class<T> type) {
            return getInstance(null, type);
        }
        
        @SuppressWarnings("unchecked")
        public <T> T getInstance(@Nullable Class<? extends Annotation> qualifier, Class<T> type) {
            Desire desire = spi.desire(qualifier, type);
            Provider<?> provider = resolver.resolve(desire);
            return (T) provider.get();
        }
    }
}
