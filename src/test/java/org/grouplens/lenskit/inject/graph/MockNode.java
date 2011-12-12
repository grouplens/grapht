/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.lenskit.inject.graph;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Node;

import com.google.common.base.Function;

/**
 * MockNode is a simple implementation of Nodes for certain types of test cases.
 * It can be full configured by its constructors, although
 * {@link #makeProvider(Function)} always returns the same provider.
 * 
 * @author Michael Ludwig
 */
public class MockNode implements Node {
    private final Provider<?> provider;
    private final Class<?> type;
    private final List<Desire> dependencies;
    
    public MockNode() {
        this(Object.class, new Object(), new ArrayList<Desire>());
    }
    
    public MockNode(Class<?> type, List<Desire> dependencies) {
        this(type, new NullProvider(), dependencies);
    }
    
    public MockNode(Class<?> type, Object instance, List<Desire> dependencies) {
        this(type, new InstanceProvider(instance), dependencies);
    }
    
    public MockNode(Class<?> type, Provider<?> provider, List<Desire> dependencies) {
        this.type = type;
        this.provider = provider;
        this.dependencies = dependencies;
    }
    
    @Override
    public List<Desire> getDependencies() {
        return dependencies;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getErasedType() {
        // FIXME: is this correct?
        return type;
    }

    @Override
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return provider;
    }

    @SuppressWarnings("rawtypes")
    private static class NullProvider implements Provider {
        @Override
        public Object get() {
            return null;
        }
    }
    
    @SuppressWarnings("rawtypes")
    private static class InstanceProvider implements Provider {
        private final Object instance;
        
        public InstanceProvider(Object instance) {
            this.instance = instance;
        }
        
        @Override
        public Object get() {
            return instance;
        }
    }
}
