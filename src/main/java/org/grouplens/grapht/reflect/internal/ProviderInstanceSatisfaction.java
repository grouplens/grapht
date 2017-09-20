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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.*;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Satisfaction implementation wrapping an existing Provider instance. It has no
 * dependencies and it always returns the same Provider when
 * {@link Satisfaction#makeInstantiator(Map, LifecycleManager)} is invoked.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ProviderInstanceSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = 1L;
    @SuppressWarnings("squid:S1948") // serializable warning; satisfaction is serializable iff provider is
    private final Provider<?> provider;

    /**
     * Create a new satisfaction that wraps the given Provider instance.
     * 
     * @param provider The provider
     * @throws NullPointerException if provider is null
     */
    public ProviderInstanceSatisfaction(Provider<?> provider) {
        Preconditions.notNull("provider", provider);
        this.provider = provider;
    }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }
    
    /**
     * @return The provider instance returned by {@link Satisfaction#makeInstantiator(Map, LifecycleManager)}
     */
    public Provider<?> getProvider() {
        return provider;
    }
    
    @Override
    public List<Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return Types.getProvidedType(provider);
    }

    @Override
    public Class<?> getErasedType() {
        return Types.erase(getType());
    }

    @Override
    public boolean hasInstance() {
        return false;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitProviderInstance(provider);
    }

    @Override
    public Instantiator makeInstantiator(Map<Desire,Instantiator> dependencies,
                                         LifecycleManager lm) {
        return Instantiators.ofProvider(provider);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o instanceof ProviderInstanceSatisfaction)
               && ((ProviderInstanceSatisfaction) o).provider.equals(provider);
    }
    
    @Override
    public int hashCode() {
        return provider.hashCode();
    }
    
    @Override
    public String toString() {
        return "ProviderInstance(" + provider + ")";
    }
}
