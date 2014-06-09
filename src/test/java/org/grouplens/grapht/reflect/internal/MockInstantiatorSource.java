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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.reflect.InstantiatorSource;

import java.util.HashMap;
import java.util.Map;

public class MockInstantiatorSource implements InstantiatorSource {
    private final Map<ReflectionDesire, Instantiator> providers;
    
    public MockInstantiatorSource() {
        providers = new HashMap<ReflectionDesire, Instantiator>();
    }
    
    public void add(ReflectionDesire desire, Instantiator provider) {
        providers.put(desire, provider);
    }
    
    public void add(InjectionPoint injectPoint, Instantiator provider) {
        providers.put(new ReflectionDesire(injectPoint), provider);
    }
    
    @Override
    public Instantiator apply(Desire desire) {
        return providers.get(desire);
    }
}
