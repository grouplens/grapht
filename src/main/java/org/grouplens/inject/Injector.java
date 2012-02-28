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

import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

public class Injector {
    private Injector() { }
    
    public static RootContext createContext() {
        return createContext(new ReflectionInjectSPI());
    }
    
    public static RootContext createContext(InjectSPI spi) {
        return new RootContextImpl(spi);
    }
    
    // FIXME: we should add a Module-esque type so that create() can take
    // modules, and each module is provided with the root context to configure
    // bindings.  Then the bindings are processed and converted into a usable
    // injector.
}
