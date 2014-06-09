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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.NullComponentException;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instantiates class instances.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ClassInstantiator implements Instantiator {
    private static final Logger logger = LoggerFactory.getLogger(ClassInstantiator.class);
    
    private final Class<?> type;
    private final List<Desire> desires;
    private final Map<Desire, Instantiator> providers;

    /**
     * Create an ClassInstantiator that will provide instances of the given
     * type, with given the list of desires and a function mapping that
     * satisfies those providers.
     * 
     * @param type The type of instance created
     * @param desires The dependency desires for the instance
     * @param providers The providers that satisfy the desires of the type
     */
    public ClassInstantiator(Class<?> type, List<Desire> desires, Map<Desire,Instantiator> providers) {
        Preconditions.notNull("type", type);
        Preconditions.notNull("desires", desires);
        Preconditions.notNull("providers", providers);
        
        this.type = type;
        this.desires = desires;
        this.providers = providers;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public Object call() throws InjectionException {
        // find constructor and build up necessary constructor arguments
        Constructor<?> ctor = getConstructor();
        Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
        for (Desire d: desires) {
            if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                // this desire is a constructor argument so create it now
                Instantiator provider = providers.get(d);
                ConstructorParameterInjectionPoint cd = (ConstructorParameterInjectionPoint) d.getInjectionPoint();
                ctorArgs[cd.getParameterIndex()] = checkNull(cd, provider.call());
            }
        }
        
        // create the instance that we are injecting
        Object instance;
        try {
            logger.trace("Invoking constructor {} with arguments {}", ctor, ctorArgs);
            ctor.setAccessible(true);
            instance = ctor.newInstance(ctorArgs);
        } catch (InvocationTargetException e) {
            throw new InjectionException(ctor, "Constructor " + ctor + " failed", e);
        } catch (InstantiationException e) {
            throw new InjectionException(ctor, "Could not instantiate " + type, e);
        } catch (IllegalAccessException e) {
            throw new InjectionException(ctor, "Access violation on " + ctor, e);
        }

        // satisfy dependencies in the order of the list, which was
        // prepared to comply with JSR 330
        Map<Method, InjectionArgs> settersAndArguments = new HashMap<Method, InjectionArgs>();
        for (Desire d: desires) {
            if (d.getInjectionPoint() instanceof FieldInjectionPoint) {
                FieldInjectionPoint fd = (FieldInjectionPoint) d.getInjectionPoint();
                Object value = checkNull(fd, providers.get(d).call());
                Field field = fd.getMember();

                try {
                    logger.trace("Setting field {} with arguments {}", field, value);
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (IllegalAccessException e) {
                    throw new InjectionException(fd, e);
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
                
                Instantiator provider = providers.get(d);
                args.set(sd.getParameterIndex(), checkNull(sd, provider.call()));
                
                if (args.isCompleted()) {
                    // all parameters initialized, invoke the setter with all arguments
                    try {
                        logger.trace("Invoking setter {} with arguments {}", setter, args.arguments);
                        setter.setAccessible(true);
                        setter.invoke(instance, args.arguments);
                    } catch (InvocationTargetException e) {
                        String message = "Exception thrown by ";
                        if (args.arguments.length == 1) {
                            message += sd;
                        } else {
                            message += setter;
                        }
                        throw new InjectionException(sd, message, e);
                    } catch (IllegalAccessException e) {
                        String message = "Access violation calling ";
                        if (args.arguments.length == 1) {
                            message += sd;
                        } else {
                            message += setter;
                        }
                        throw new InjectionException(sd, message, e);
                    }
                }
            } else if (d.getInjectionPoint() instanceof NoArgumentInjectionPoint) {
                // just invoke the method
                Method method = ((NoArgumentInjectionPoint) d.getInjectionPoint()).getMember();
                try {
                    logger.trace("Invoking no-argument injection point {}", d.getInjectionPoint());
                    method.setAccessible(true);
                    method.invoke(instance);
                } catch (InvocationTargetException e) {
                    throw new InjectionException(d.getInjectionPoint(), "Exception throw by " + method, e);
                } catch (IllegalAccessException e) {
                    throw new InjectionException(d.getInjectionPoint(), "Access violation invoking " + method, e);
                }
            }
        }
        
        // the instance has been fully configured
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private Constructor<?> getConstructor() {
        for (Desire d: desires) {
            if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                // since we only allow one injectable constructor, any ConstructorParameterInjectionPoint
                // will have the same constructor as all other constructor parameter injection points
                Constructor<?> ctor = ((ConstructorParameterInjectionPoint) d.getInjectionPoint()).getMember();
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
    
    private static Object checkNull(InjectionPoint injectPoint, Object value) throws NullComponentException {
        if (value == null && !injectPoint.isNullable()) {
            throw new NullComponentException(injectPoint);
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
