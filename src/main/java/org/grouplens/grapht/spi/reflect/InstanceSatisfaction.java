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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.util.InstanceProvider;

/**
 * Satisfaction implementation wrapping an instance. It has no dependencies, and
 * the resulting providers just return the instance.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class InstanceSatisfaction extends ReflectionSatisfaction {
    private final Object instance;

    /**
     * Create a new instance node wrapping an instance.
     * 
     * @param obj The object to return.
     * @throws NullPointerException if obj is null
     */
    public InstanceSatisfaction(Object obj) {
        Checks.notNull("instance", obj);
        instance = obj;
    }
    
    /**
     * @return The instance that satisfies this satisfaction
     */
    public Object getInstance() {
        return instance;
    }

    @Override
    public List<? extends Desire> getDependencies() {
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Provider<?> makeProvider(ProviderSource dependencies) {
        return new InstanceProvider(instance);
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
