/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.*;

import javax.inject.Provider;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * MockSatisfaction is a simple implementation of Satisfactions for certain
 * types of test cases. It can be configured by its constructors, although
 * {@link Satisfaction#makeInstantiator(Map, LifecycleManager)} always returns the same provider.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MockSatisfaction implements Satisfaction {
    private static final long serialVersionUID = 1L;

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
    public CachePolicy getDefaultCachePolicy() {
        return CachePolicy.NO_PREFERENCE;
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
        return type;
    }

    @Override
    public boolean hasInstance() {
        return true;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        throw new UnsupportedOperationException("cannot visit the mock satisfaction");
    }

    @Override
    public Instantiator makeInstantiator(Map<Desire,Instantiator> dependencies,
                                         LifecycleManager lm) {
        return Instantiators.ofProvider(provider);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb.append("mock satisfaction of ")
                 .append(type)
                 .append(" with provider ")
                 .append(provider)
                 .append(" (")
                 .append(dependencies.size())
                 .append(" dependencies)")
                 .toString();
    }

    @SuppressWarnings("rawtypes")
    private static class NullProvider implements Provider {
        @Override
        public Object get() {
            return null;
        }

        @Override
        public String toString() {
            return "return null";
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

        @Override
        public String toString() {
            return "return " + instance;
        }
    }
}
