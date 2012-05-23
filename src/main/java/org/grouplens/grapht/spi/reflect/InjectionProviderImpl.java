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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.spi.ProviderSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InjectionProviderImpl is a Provider implementation capable of creating any
 * type assuming that the type can be represented as a set of desires, and that
 * those desires are satisfied by other Provider implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The object type that is provided
 */
public class InjectionProviderImpl<T> implements Provider<T> {
    private static final Logger logger = LoggerFactory.getLogger(InjectionProviderImpl.class);
    
    private final Class<T> type;
    private final List<ReflectionDesire> desires;
    private final ProviderSource providers;

    /**
     * Create an InjectionProviderImpl that will provide instances of the given
     * type, with given the list of desires and a function mapping that
     * satisfies those providers.
     * 
     * @param type The type of instance created
     * @param desires The dependency desires for the instance
     * @param providers The providers that satisfy the desires of the type
     */
    public InjectionProviderImpl(Class<T> type, List<ReflectionDesire> desires, ProviderSource providers) {
        Checks.notNull("type", type);
        Checks.notNull("desires", desires);
        Checks.notNull("providers", providers);
        
        this.type = type;
        this.desires = desires;
        this.providers = providers;
    }

    @Override
    public T get() {
        // find constructor and build up necessary constructor arguments
        Constructor<T> ctor = getConstructor();
        Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
        for (ReflectionDesire d: desires) {
            if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                // this desire is a constructor argument so create it now
                Provider<?> provider = providers.apply(d);
                ConstructorParameterInjectionPoint cd = (ConstructorParameterInjectionPoint) d.getInjectionPoint();
                ctorArgs[cd.getParameterIndex()] = checkNull(cd, provider.get());
            }
        }
        
        // create the instance that we are injecting
        T instance;
        try {
            logger.trace("Invoking constructor {} with arguments {}", ctor, ctorArgs);
            instance = ctor.newInstance(ctorArgs);
        } catch (Exception e) {
            throw new InjectionException(type, ctor, e);
        }
        
        // complete injection by satisfying any setter method dependencies
        Map<Method, Object[]> settersAndArguments = new HashMap<Method, Object[]>();
        for (ReflectionDesire d: desires) {
            if (d.getInjectionPoint() instanceof SetterInjectionPoint) {
                SetterInjectionPoint sd = (SetterInjectionPoint) d.getInjectionPoint();
                Object[] args = settersAndArguments.get(sd.getMember());
                
                if (args == null) {
                    // first encounter of this method
                    args = new Object[sd.getMember().getParameterTypes().length];
                    settersAndArguments.put(sd.getMember(), args);
                }
                
                Provider<?> provider = providers.apply(d);
                args[sd.getParameterIndex()] = checkNull(sd, provider.get());
            }
        }
        
        // invoke all completed setter methods
        for (Method setter: settersAndArguments.keySet()) {
            try {
                logger.trace("Invoking setter {} with arguments {}", setter, settersAndArguments.get(setter));
                setter.invoke(instance, settersAndArguments.get(setter));
            } catch (Exception e) {
                throw new InjectionException(type, setter, e);
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
                Constructor<T> ctor = (Constructor<T>) ((ConstructorParameterInjectionPoint) d.getInjectionPoint()).getMember();
                logger.debug("Using constructor annotated with @Inject: {}", ctor);
                return ctor;
            }
        }
        
        try {
            logger.debug("Using default constructor for {}", type);
            return type.getConstructor();
        } catch (NoSuchMethodException e) {
            // this constructor is being invoked for a ClassSatisfaction or a 
            // ProviderClassSatisfaction, both of which assert that the type is
            // instantiable, so this should never happen
            throw new RuntimeException("Unexpected exception", e);
        }
    }
    
    private static Object checkNull(InjectionPoint injectPoint, Object value) {
        if (value == null && !injectPoint.isNullable()) {
            throw new InjectionException(injectPoint.getMember().getDeclaringClass(),
                                         injectPoint.getMember(), 
                                         "Injection point is not annotated with @Nullable, but binding configuration provided a null value");
        } else {
            return value;
        }
    }
}
