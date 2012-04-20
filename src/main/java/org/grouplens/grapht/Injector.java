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

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import org.grouplens.grapht.spi.InjectSPI;

/**
 * <p>
 * Injector uses dependency injection to act as a factory for creating instances
 * with complex dependencies. A default implementation of Injector can easily be
 * created by using an {@link InjectorBuilder}:
 * 
 * <pre>
 * InjectorBuilder b = new InjectorBuilder();
 * b.bind(Foo.class).to(Bar.class);
 * b.applyModule(new MyCustomModule());
 * // other bindings
 * Injector i = b.build();
 * assert (i.getInstance(Foo.class) instanceof Bar);
 * </pre>
 * <p>
 * Alternatively, {@link InjectorConfigurationBuilder}, and {@link InjectSPI}
 * can be used to create your own Injector implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Injector {
    /**
     * <p>
     * Get an instance of T based on the bindings that this Injector was
     * configured with. An exception is thrown if the request type cannot be
     * instantiated with dependency injection.
     * <p>
     * Injectors may memoize or cache previously created objects. As an example,
     * the Injector created by {@link InjectorBuilder} reuses instances where
     * possible.
     * 
     * @param <T> The object type being created
     * @param type The class type
     * @return An instance of type T
     */
    public <T> T getInstance(Class<T> type);

    /**
     * <p>
     * Get an instance of T with the given {@link Qualifier} annotation.
     * 
     * @param <T> The object type
     * @param qualifier The qualifier on of the returned instance
     * @param type The class type
     * @return An instance of type T
     */
    public <T> T getInstance(Annotation qualifier, Class<T> type);
}
