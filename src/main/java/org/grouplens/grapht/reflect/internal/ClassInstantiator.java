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

import com.google.common.collect.ImmutableList;
import org.grouplens.grapht.ConstructionException;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.LifecycleManager;
import org.grouplens.grapht.NullDependencyException;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.LogContext;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

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
    private final LifecycleManager manager;

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
                             LifecycleManager manager) {
        Preconditions.notNull("type", type);
        Preconditions.notNull("desires", desires);
        Preconditions.notNull("providers", providers);

        this.type = type;
        this.desires = desires;
        this.providers = providers;
        this.manager = manager;
    }

    @Override
    public Class getType() {
        return type;
    }

    @Override
    public Object instantiate() throws ConstructionException {
        // find constructor and build up necessary constructor arguments

        Map<Member, List<Desire>> depGroups =
                desires.stream()
                       .collect(Collectors.groupingBy(d -> d.getInjectionPoint().getMember()));

        Constructor<?> ctor = getConstructor();
        Object instance = null;

        try (LogContext globalLogContext = LogContext.create()) {
            globalLogContext.put("org.grouplens.grapht.class", ctor.getClass().toString());
            instance = createInstance(ctor, depGroups.getOrDefault(ctor, Collections.emptyList()));

            // JSR 330 requires supertype injection points to be set first, and fields first
            List<Member> members = new ArrayList<>(depGroups.keySet());
            members.remove(ctor);
            assert members.stream().noneMatch(m -> m instanceof Constructor);
            members.sort(Comparator.comparing(Member::getDeclaringClass,
                                              Types.supertypesFirst())
                                   .thenComparing((m1, m2) -> {
                                       if (m1 instanceof Field && m2 instanceof Method) {
                                           return -1;
                                       } else if (m1 instanceof Method && m2 instanceof Field) {
                                           return 1;
                                       } else {
                                           return 0;
                                       }
                                   }));

            Map<Method, InjectionArgs> settersAndArguments = new HashMap<Method, InjectionArgs>();
            for (Member m: members) {
                if (m instanceof Method) {
                    invokeMethod(instance, (Method) m, depGroups.get(m));
                } else if (m instanceof Field) {
                    invokeField(instance, (Field) m, depGroups.get(m));
                } else {
                    throw new IllegalStateException("unexpected member " + m);
                }
            }
        }

        if (manager != null) {
            manager.registerComponent(instance);
        }

        for(Method method: getPostConstructMethods()){
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

    private List<Method> getPostConstructMethods() {
        ImmutableList.Builder<Method> methods = ImmutableList.builder();

        Types.getUniqueMethods(type)
             .stream()
             .filter(m -> m.getAnnotation(Inject.class) != null)
             .filter(m -> m.getParameterCount() == 0)
             .sorted(Comparator.comparing(Method::getDeclaringClass,
                                          Types.supertypesFirst()))
             .forEach(methods::add);

        return methods.build();
    }

    private Object createInstance(Constructor<?> ctor, List<Desire> ctorDeps) throws ConstructionException {
        Object instance;
        try {
            Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
            for (Desire d : ctorDeps) {
                // this desire is a constructor argument so create it now
                Instantiator provider = providers.get(d);
                InjectionPoint ip = d.getInjectionPoint();
                logger.trace("Injection point satisfactions in progress {}", ip);
                try (LogContext ipContext = LogContext.create()) {
                    ipContext.put("org.grouplens.grapht.injectionPoint", ip.toString());
                    ctorArgs[ip.getParameterIndex()] = ip.transform(checkNull(ip, provider.instantiate()));
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
        return instance;
    }

    private void invokeMethod(Object instance, Method setter, List<Desire> desires) throws ConstructionException {
        Object[] args = new Object[desires.size()];
        desires.sort(Comparator.comparing(d -> d.getInjectionPoint().getParameterIndex()));
        for (int i = 0; i < args.length; i++) {
            Desire d = desires.get(i);
            InjectionPoint ip = d.getInjectionPoint();
            try (LogContext ipContext = LogContext.create()) {
                ipContext.put("org.grouplens.grapht.injectionPoint", d.getInjectionPoint().toString());
                args[i] = ip.transform(checkNull(ip, providers.get(d).instantiate()));
            }
        }

        try {
            logger.trace("Invoking setter {} with arguments {}", setter, args);
            setter.setAccessible(true);
            setter.invoke(instance, args);
        } catch (InvocationTargetException e) {
            String message = "Exception thrown by ";
            if (args.length == 1) {
                message += desires.get(0).getInjectionPoint();
            } else {
                message += setter;
            }
            throw new ConstructionException(desires.get(0).getInjectionPoint(), message, e);
        } catch (IllegalAccessException e) {
            String message = "Access violation calling ";
            if (args.length == 1) {
                message += desires.get(0).getInjectionPoint();
            } else {
                message += setter;
            }
            throw new ConstructionException(desires.get(0).getInjectionPoint(), message, e);
        }
    }

    private void invokeField(Object instance, Field field, List<Desire> desires) throws ConstructionException {
        assert desires.size() == 1;
        Object value;
        Desire d = desires.get(0);
        Instantiator provider = providers.get(d);
        try (LogContext ipContext = LogContext.create()) {
            ipContext.put("org.grouplens.grapht.injectionPoint", d.getInjectionPoint().toString());
            value = ClassInstantiator.checkNull(d.getInjectionPoint(), provider.instantiate());
            logger.trace("Setting field {} with arguments {}", field, value);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new ConstructionException(d.getInjectionPoint(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Constructor<?> getConstructor() {
        for (Desire d: desires) {
            if (d.getInjectionPoint().getMember() instanceof Constructor) {
                // since we only allow one injectable constructor, any constructor injection point
                // will have the same constructor as all other constructor parameter injection points
                Constructor<?> ctor = (Constructor<?>) d.getInjectionPoint().getMember();
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
            throw new IllegalStateException("Unexpected exception", e);
        }
    }

    static Object checkNull(InjectionPoint injectPoint, Object value) throws NullDependencyException {
        if (value == null && !injectPoint.isOptional()) {
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
