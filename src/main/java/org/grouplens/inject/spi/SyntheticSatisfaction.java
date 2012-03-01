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
package org.grouplens.inject.spi;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import com.google.common.base.Function;

public class SyntheticSatisfaction implements Satisfaction {
    private final List<Desire> desires;
    
    public SyntheticSatisfaction(Desire... desires) {
        this.desires = Arrays.asList(desires);
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.unmodifiableList(desires);
    }

    @Override
    public Type getType() {
        return null; // synthetic
    }

    @Override
    public Class<?> getErasedType() {
        return null; // synthetic
    }

    @Override
    public Provider<Map<Desire, Provider<?>>> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return new SyntheticProvider(dependencies);
    }

    @Override
    public Comparator<ContextMatcher> contextComparator(Role role) {
        return new Comparator<ContextMatcher>() {
            @Override
            public int compare(ContextMatcher o1, ContextMatcher o2) {
                return 0;
            }
        };
    }
    
    private class SyntheticProvider implements Provider<Map<Desire, Provider<?>>> {
        private final Function<? super Desire, ? extends Provider<?>> mapping;
        
        public SyntheticProvider(Function<? super Desire, ? extends Provider<?>> mapping) {
            this.mapping = mapping;
        }
        
        @Override
        public Map<Desire, Provider<?>> get() {
            Map<Desire, Provider<?>> map = new HashMap<Desire, Provider<?>>();
            for (Desire d: desires) {
                map.put(d, mapping.apply(d));
            }
            return map;
        }
    }
}
