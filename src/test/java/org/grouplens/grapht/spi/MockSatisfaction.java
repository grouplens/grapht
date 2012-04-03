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
package org.grouplens.grapht.spi;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.Satisfaction;


import com.google.common.base.Function;

/**
 * MockSatisfaction is a simple implementation of Satisfactions for certain
 * types of test cases. It can be configured by its constructors, although
 * {@link #makeProvider(Function)} always returns the same provider.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockSatisfaction implements Satisfaction {
    private final Provider<?> provider;
    private final Class<?> type;
    private final List<Desire> dependencies;
    
    public MockSatisfaction() {
        this(Object.class, new Object(), new ArrayList<Desire>());
    }
    
    public MockSatisfaction(Class<?> type) {
        this(type, new ArrayList<Desire>());
    }
    
    public MockSatisfaction(Class<?> type, List<Desire> dependencies) {
        this(type, new NullProvider(), dependencies);
    }
    
    public MockSatisfaction(Class<?> type, Object instance, List<Desire> dependencies) {
        this(type, new InstanceProvider(instance), dependencies);
    }
    
    public MockSatisfaction(Class<?> type, Provider<?> provider, List<Desire> dependencies) {
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
    
    @Override
    public Comparator<ContextMatcher> contextComparator(Qualifier qualifier) {
        return new Comparator<ContextMatcher>() {
            @Override
            public int compare(ContextMatcher arg0, ContextMatcher arg1) {
                return 0;
            }
        };
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
