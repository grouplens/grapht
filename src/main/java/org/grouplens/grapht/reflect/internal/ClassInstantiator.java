
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

import org.grouplens.grapht.*;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.LogContext;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;

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
    private final InjectionContainer container;
    private Method[] methods;
    Annotation[] annotations;


    /**
     * Create an ClassInstantiator that will provide instances of the given
     * type, with given the list of desires and a function mapping that
     * satisfies those providers.
     *
     * @param type The type of instance created
     * @param desires The dependency desires for the instance
     * @param providers The providers that satisfy the desires of the type
     */
    public ClassInstantiator(Class<?> type, List<Desire> desires,
                             Map<Desire,Instantiator> providers,
                             InjectionContainer container) {
        Preconditions.notNull("type", type);
        Preconditions.notNull("desires", desires);
        Preconditions.notNull("providers", providers);
        Preconditions.notNull("injectionContainer", container);

        this.type = type;
        this.desires = desires;
        this.providers = providers;
        this.container = container;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public Object instantiate() throws ConstructionException {
        // find constructor and build up necessary constructor arguments

        Constructor<?> ctor = getConstructor();
        LogContext globalLogContext = LogContext.create();
        Object instance = null;
        try {
            // create the instance that we are injecting
            try {
                globalLogContext.put("org.grouplens.grapht.class", ctor.getClass().toString());
                Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
                for (Desire d : desires) {
                    LogContext ipContext = LogContext.create();
                    if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                        // this desire is a constructor argument so create it now
                        Instantiator provider = providers.get(d);
                        ConstructorParameterInjectionPoint cd = (ConstructorParameterInjectionPoint) d.getInjectionPoint();
                        logger.trace("Injection point satisfactions in progress {}", cd);
                        try {
                            ipContext.put("org.grouplens.grapht.injectionPoint", cd.toString());
                        } finally {
                            ipContext.finish();
                        }
                        ctorArgs[cd.getParameterIndex()] = checkNull(cd, provider.instantiate());
                    }
                }
                logger.trace("Invoking constructor {} with arguments {}", ctor, ctorArgs);
                ctor.setAccessible(true);
                instance = ctor.newInstance(ctorArgs);
            } catch (InvocationTargetException e) {
                throw new ConstructionException(ctor, "Constructor " + ctor + " failed", e);
            } catch (InstantiationException e) {
                throw new ConstructionException(ctor, "Could not instantiate " + type, e);
            } catch (IllegalAccessException e) {
                throw new ConstructionException(ctor, "Access violation on " + ctor, e);
            }

            // satisfy dependencies in the order of the list, which was
            // prepared to comply with JSR 330
            Map<Method, InjectionArgs> settersAndArguments = new HashMap<Method, InjectionArgs>();
            for (Desire d : desires) {
                LogContext ipContext = LogContext.create();
                try {
                    final InjectionStrategy injectionStrategy = InjectionStrategy.forInjectionPoint(d.getInjectionPoint());
                    ipContext.put("org.grouplens.grapht.injectionPoint", d.getInjectionPoint().toString());
                    injectionStrategy.inject(d.getInjectionPoint(), instance, providers.get(d), settersAndArguments);
                } finally {
                    ipContext.finish();
                }
            }
        } finally {
            globalLogContext.finish();
        }
        container.registerComponent(instance);

        methods = MethodUtils.getMethodsWithAnnotation(type, PostConstruct.class);
        for(Method method:methods){
            method.setAccessible(true);
            try {
                method.invoke(instance);
            } catch (InvocationTargetException e) {
                throw new ConstructionException("Exception throw by " + method, e);
            } catch (IllegalAccessException e) {
                throw new ConstructionException("Access violation invoking " + method, e);
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

    static Object checkNull(InjectionPoint injectPoint, Object value) throws NullDependencyException {
        if (value == null && !injectPoint.isNullable()) {
            throw new NullDependencyException(injectPoint);
        } else {
            return value;
        }
    }

    static class InjectionArgs {
        public final Object[] arguments;
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
