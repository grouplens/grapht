/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.InjectionContainer;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.Instantiators;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.SatisfactionVisitor;
import org.grouplens.grapht.util.Preconditions;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Satisfaction implementation wrapping an instance. It has no dependencies, and
 * the resulting providers just return the instance.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InstanceSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = 1L;
    private final Object instance;

    /**
     * Create a new instance node wrapping an instance.
     * 
     * @param obj The object to return.
     * @throws NullPointerException if obj is null
     */
    public InstanceSatisfaction(Object obj) {
        Preconditions.notNull("instance", obj);
        instance = obj;
    }
    
    /**
     * @return The instance that satisfies this satisfaction
     */
    public Object getInstance() {
        return instance;
    }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }

    @Override
    public List<Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return instance.getClass();
    }

    @Override
    public Class<?> getErasedType() {
        return instance.getClass();
    }

    @Override
    public boolean hasInstance() {
        return true;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitInstance(instance);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Instantiator makeInstantiator(Map<Desire,Instantiator> dependencies,
                                         InjectionContainer injectionContainer) {
        return Instantiators.ofInstance(instance);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof InstanceSatisfaction)) {
            return false;
        }
        return ((InstanceSatisfaction) o).instance.equals(instance);
    }
    
    @Override
    public int hashCode() {
        return instance.hashCode();
    }
    
    @Override
    public String toString() {
        return "Instance(" + instance + ")";
    }
}
