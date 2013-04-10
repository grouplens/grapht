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

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Preconditions.notNull("type", type);
        Preconditions.notNull("desires", desires);
        Preconditions.notNull("providers", providers);
        
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
            ctor.setAccessible(true);
            instance = ctor.newInstance(ctorArgs);
        } catch (Exception e) {
            throw new InjectionException(type, ctor, e);
        }
        
        // satisfy dependencies in the order of the list, which was
        // prepared to comply with JSR 330
        Map<Method, InjectionArgs> settersAndArguments = new HashMap<Method, InjectionArgs>();
        for (ReflectionDesire d: desires) {
            if (d.getInjectionPoint() instanceof FieldInjectionPoint) {
                FieldInjectionPoint fd = (FieldInjectionPoint) d.getInjectionPoint();
                Object value = checkNull(fd, providers.apply(d).get());
                Field field = fd.getMember();

                try {
                    logger.trace("Setting field {} with arguments {}", field, value);
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (Exception e) {
                    throw new InjectionException(type, fd.getMember(), e);
                }
            } else if (d.getInjectionPoint() instanceof SetterInjectionPoint) {
                // collect parameters before invoking
                SetterInjectionPoint sd = (SetterInjectionPoint) d.getInjectionPoint();
                InjectionArgs args = settersAndArguments.get(sd.getMember());
                Method setter = sd.getMember();

                if (args == null) {
                    // first encounter of this method
                    args = new InjectionArgs(setter.getParameterTypes().length);
                    settersAndArguments.put(setter, args);
                }
                
                Provider<?> provider = providers.apply(d);
                args.set(sd.getParameterIndex(), checkNull(sd, provider.get()));
                
                if (args.isCompleted()) {
                    // all parameters initialized, invoke the setter with all arguments
                    try {
                        logger.trace("Invoking setter {} with arguments {}", setter, args.arguments);
                        setter.setAccessible(true);
                        setter.invoke(instance, args.arguments);
                    } catch (Exception e) {
                        throw new InjectionException(type, setter, e);
                    }
                }
            } else if (d.getInjectionPoint() instanceof NoArgumentInjectionPoint) {
                // just invoke the method
                Method method = ((NoArgumentInjectionPoint) d.getInjectionPoint()).getMember();
                try {
                    logger.trace("Invoking no-argument injection point {}", d.getInjectionPoint());
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (Exception e) {
                    throw new InjectionException(type, method, e);
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
                Constructor<T> ctor = (Constructor<T>) ((ConstructorParameterInjectionPoint) d.getInjectionPoint()).getMember();
                logger.debug("Using constructor annotated with @Inject: {}", ctor);
                return ctor;
            }
        }
        
        try {
            logger.debug("Using default constructor for {}", type);
            return type.getDeclaredConstructor();
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
    
    private static class InjectionArgs {
        private final Object[] arguments;
        private final boolean[] injected;
        
        public InjectionArgs(int num) {
            arguments = new Object[num];
            injected = new boolean[num];
        }
        
        public void set(int i, Object o) {
            arguments[i] =o;
            injected[i] = true;
        }
        
        public boolean isCompleted() {
            for (int i = 0; i < injected.length; i++) {
                if (!injected[i]) {
                    return false;
                }
            }
            return true;
        }
    }
}
