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

import org.grouplens.grapht.ConstructionException;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.NullDependencyException;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.InjectionPointVisitor;
import org.grouplens.grapht.util.LogContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Helper methods for instantiate() method
 * ClassInstantiator
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectionPointVisitorImpl implements InjectionPointVisitor {

    private static final Logger logger = LoggerFactory.getLogger(InjectionPointVisitorImpl.class);
    private InjectionPoint injectionPoint;
    private Object instance;
    private Map<Desire, Instantiator> providers;
    Desire d;
    Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments = null;

    InjectionPointVisitorImpl(Desire d, InjectionPoint injectionPoint, Map<Desire, Instantiator> providers, Object instance,
                              Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments) {
        this.d                   =  d;
        this.injectionPoint      =  d.getInjectionPoint();
        this.providers           =  providers;
        this.instance            =  instance;
        this.settersAndArguments = settersAndArguments;
    }

    @Override
    public void visitField(FieldInjectionPoint fd) throws ConstructionException {
        LogContext mdcContextInjectionPtField =  LogContext.create();
        Field field;
        Object value;
        try {
            mdcContextInjectionPtField.put("org.grouplens.grapht.injectionPoint", injectionPoint.toString());
            value = ClassInstantiator.checkNull(fd, providers.get(d).instantiate());
            field = fd.getMember();
            logger.trace("Setting field {} with arguments {}", field, value);
            field.setAccessible(true);
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new ConstructionException(fd, e);

        } catch (NullDependencyException e) {
            throw new ConstructionException(fd, e);
        } finally {
            mdcContextInjectionPtField.finish();
        }

    }

    @Override
    public void visitSetter(SetterInjectionPoint st) throws ConstructionException {
        ClassInstantiator.InjectionArgs args = settersAndArguments.get(st.getMember());
        Method setter = st.getMember();
        if (args == null) {
        //  first encounter of this method

            args = new ClassInstantiator.InjectionArgs(setter.getParameterTypes().length);
            settersAndArguments.put(setter, args);
        }
        Instantiator provider = providers.get(d);
        LogContext mdcContextSetterInjectionPoint = LogContext.create();
        try {
            mdcContextSetterInjectionPoint.put("org.grouplens.grapht.injectionPoint", st.toString());
            args.set(st.getParameterIndex(), ClassInstantiator.checkNull(st, provider.instantiate()));
        } catch (Throwable th) {
            throw new RuntimeException("Unexpected instantiation exception", th);

        } finally {
            mdcContextSetterInjectionPoint.finish();
        }
        if (args.isCompleted()) {
        // all parameters initialized, invoke the setter with all arguments
            try {
                logger.trace("Invoking setter {} with arguments {}", setter, args.arguments);
                setter.setAccessible(true);
                setter.invoke(instance, args.arguments);
            } catch (InvocationTargetException e) {
                String message = "Exception thrown by ";
                if (args.arguments.length == 1) {
                    message += st;
                } else {
                    message += setter;
                }
                throw new ConstructionException(st, message, e);
            } catch (IllegalAccessException e) {
                String message = "Access violation calling ";
                if (args.arguments.length == 1) {
                    message += st;
                } else {
                    message += setter;
                }
                throw new ConstructionException(st, message, e);
            }
        }
    }

    @Override
    public void visitNoArgument(NoArgumentInjectionPoint ip) throws ConstructionException {
        Method method = null;
        LogContext mdcContextInjectionPtNoArgs = LogContext.create();
        try {
            method = ip.getMember();
            logger.trace("Injection point method with no argument in progress {}", injectionPoint);
            mdcContextInjectionPtNoArgs.put("org.grouplens.grapht.injectionPoint", injectionPoint.toString());
            logger.trace("Invoking no-argument injection point {}", injectionPoint);
            method.setAccessible(true);
            method.invoke(instance);
        } catch (InvocationTargetException e) {
            throw new ConstructionException(injectionPoint, "Exception throw by " + method, e);
        } catch (IllegalAccessException e) {
            throw new ConstructionException(injectionPoint, "Access violation invoking " + method, e);
        } finally {
            mdcContextInjectionPtNoArgs.finish();
        }
    }

    @Override
    public void visitConstructor(ConstructorParameterInjectionPoint ip) throws ConstructionException {

    }

    @Override
    public void visitSynthetic(SimpleInjectionPoint ip) throws InjectionException {

    }
}
