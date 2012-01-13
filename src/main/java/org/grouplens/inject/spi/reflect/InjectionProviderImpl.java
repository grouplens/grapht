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
package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

/**
 * InjectionProviderImpl is a Provider implementation capable of creating any
 * type assuming that the type can be represented as a set of desires, and that
 * those desires are satisfied by other Provider implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The object type that is provided
 */
// FIXME: we need better exceptions for dependency failures
public class InjectionProviderImpl<T> implements Provider<T> {
    private final Class<T> type;
    private final List<ReflectionDesire> desires;
    private final Function<? super Desire, ? extends Provider<?>> providers;

    /**
     * Create an InjectionProviderImpl that will provide instances of the given
     * type, with given the list of desires and a function mapping that
     * satisfies those providers.
     * 
     * @param type The type of instance created
     * @param desires The dependency desires for the instance
     * @param providers The providers that satisfy the desires of the type
     */
    public InjectionProviderImpl(Class<T> type, List<ReflectionDesire> desires, Function<? super Desire, ? extends Provider<?>> providers) {
        this.type = type;
        this.desires = desires;
        this.providers = providers;
    }

    @Override
    public T get() {
        // find constructor and build up necessary constructor arguments
        Constructor<T> ctor = getConstructor();
        Object[] args = new Object[ctor.getParameterTypes().length];
        for (ReflectionDesire d: desires) {
            if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                // this desire is a constructor argument so create it now
                Provider<?> provider = providers.apply(d);
                if (provider == null) {
                    throw new RuntimeException("Unable to satisfy dependency");
                }
                
                ConstructorParameterInjectionPoint cd = (ConstructorParameterInjectionPoint) d.getInjectionPoint();
                args[cd.getConstructorParameter()] = provider.get();
            }
        }
        
        // create the instance that we are injecting
        T instance;
        try {
            instance = ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create instance", e);
        }
        
        // complete injection by satisfying any setter method dependencies
        for (ReflectionDesire d: desires) {
            if (d.getInjectionPoint() instanceof SetterInjectionPoint) {
                Provider<?> provider = providers.apply(d);
                if (provider == null) {
                    throw new RuntimeException("Unable to satisfy dependency");
                }
                
                try {
                    SetterInjectionPoint sd = (SetterInjectionPoint) d.getInjectionPoint();
                    sd.getSetterMethod().invoke(instance, provider.get());
                } catch (Exception e) {
                    throw new RuntimeException("Unable to inject setter dependency", e);
                }
            }
        }
        
        // the instance has been fully configured
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private Constructor<T> getConstructor() {
        for (ReflectionDesire d: desires) {
            if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                // since we only allow one injectable constructor, any ConstructorParameterInjectionPoint
                // will have the same constructor as all other constructor parameter injection points
                return (Constructor<T>) ((ConstructorParameterInjectionPoint) d.getInjectionPoint()).getConstructor();
            }
        }
        
        try {
            return type.getConstructor();
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor for type: " + type, e);
        }
    }
}
