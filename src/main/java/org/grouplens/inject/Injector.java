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

import javax.annotation.Nullable;
import javax.inject.Provider;

import org.grouplens.inject.annotation.Role;
import org.grouplens.inject.resolver.DefaultResolver;
import org.grouplens.inject.resolver.Resolver;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

// FIXME: Injector should be an interface, SimpleInjector should be an injector
// that takes a Resolver and spi
// FIXME: Well that actually depends on what we want the basic entry point to
// look like for every day use.
//   Injector.create(...)
//   new Injector(...)
//   Lenskit.injector(...)
//   Config.injector(...)
// Or have an InjectorBuilder that implements Context, wraps an InjectorConfigurationBuilder
//   and combines everything to create an Injector
public class Injector {
    private final InjectSPI spi;
    private final Resolver resolver;
    
    public Injector(Resolver resolver) {
        this(resolver, new ReflectionInjectSPI());
    }
    
    public Injector(Resolver resolver, InjectSPI spi) {
        this.resolver = resolver;
        this.spi = spi;
    }
    
    public <T> T getInstance(Class<T> type) {
        return getInstance(null, type);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getInstance(@Nullable Class<? extends Role> role, Class<T> type) {
        Desire desire = spi.desire(role, type);
        Provider<?> provider = resolver.resolve(desire);
        return (T) provider.get();
    }
    
    public static Injector create(Module... modules) {
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder();
        for (Module m: modules) {
            builder.addModule(m);
        }
        return new Injector(new DefaultResolver(builder.build()));
    }
}
