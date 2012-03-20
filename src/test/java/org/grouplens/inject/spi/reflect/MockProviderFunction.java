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
package org.grouplens.inject.spi.reflect;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

public class MockProviderFunction implements Function<Desire, Provider<?>> {
    private final Map<ReflectionDesire, Provider<?>> providers;
    
    public MockProviderFunction() {
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
