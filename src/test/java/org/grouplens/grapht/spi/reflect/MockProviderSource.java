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
package org.grouplens.grapht.spi.reflect;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;

public class MockProviderSource implements ProviderSource {
    private final Map<ReflectionDesire, Provider<?>> providers;
    
    public MockProviderSource() {
        providers = new HashMap<ReflectionDesire, Provider<?>>();
    }
    
    public void add(ReflectionDesire desire, Provider<?> provider) {
        providers.put(desire, provider);
    }
    
    public void add(InjectionPoint injectPoint, Provider<?> provider) {
        providers.put(new ReflectionDesire(injectPoint), provider);
    }
    
    @Override
    public Provider<?> apply(Desire desire) {
        return providers.get(desire);
    }
}
