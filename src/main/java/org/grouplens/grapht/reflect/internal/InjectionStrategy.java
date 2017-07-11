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

import org.grouplens.grapht.ConstructionException;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Helper methods for instantiate() method
 * ClassInstantiator
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
enum InjectionStrategy {

    // method invoked for Field-Injection type 
    FIELD {
        @Override
        public void inject(InjectionPoint ip, Object instance, Instantiator provider,
                               Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments)
                               throws ConstructionException {
            Field field;
            Object value;
            FieldInjectionPoint fd = (FieldInjectionPoint)ip;
            try {
                value = ClassInstantiator.checkNull(fd, provider.instantiate());
                field = fd.getMember();
                logger.trace("Setting field {} with arguments {}", field, value);
                field.setAccessible(true);
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new ConstructionException(fd, e);
            }
        }
    },

    // method invoked for Setter-Injection type
    SETTER {
        @Override
        public void inject(InjectionPoint ip, Object instance, Instantiator provider,
                               Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments)
                               throws ConstructionException {
            SetterInjectionPoint st = (SetterInjectionPoint)ip;
            ClassInstantiator.InjectionArgs args = settersAndArguments.get(st.getMember());
            Method setter = st.getMember();
            if (args == null) {
                //first encounter of this method
                args = new ClassInstantiator.InjectionArgs(setter.getParameterTypes().length);
                settersAndArguments.put(setter, args);
            }
                args.set(st.getParameterIndex(), ClassInstantiator.checkNull(st, provider.instantiate()));

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
    },

    // method invoked for NoArgument-Injection type.
    NOARGUMENT {
        @Override
        public void inject(InjectionPoint ip, Object instance, Instantiator provider,
                              Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments)
                              throws ConstructionException {
            Method method = null;
            NoArgumentInjectionPoint noArugment = (NoArgumentInjectionPoint)ip;
            try {
                method = noArugment.getMember();
                logger.trace("Injection point method with no argument in progress {}",noArugment);
                logger.trace("Invoking no-argument injection point {}", ip);
                method.setAccessible(true);
                method.invoke(instance);
            } catch (InvocationTargetException e) {
                throw new ConstructionException(ip, "Exception throw by " + method, e);
            } catch (IllegalAccessException e) {
                throw new ConstructionException(ip, "Access violation invoking " + method, e);
            }
        }
    },

    DEFAULTCASE {
        @Override
        public void inject(InjectionPoint ip, Object instance, Instantiator provider,
                              Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments)
                              throws ConstructionException {
        }
    };

    private static final Logger logger = LoggerFactory.getLogger(InjectionStrategy.class);

    public abstract void inject(InjectionPoint ip, Object instance, Instantiator provider,
                                   Map<Method, ClassInstantiator.InjectionArgs> settersAndArguments)
                                   throws ConstructionException;

    public static InjectionStrategy forInjectionPoint(InjectionPoint ip) {
        if(ip instanceof FieldInjectionPoint) {
            return InjectionStrategy.FIELD;
        }
        else if(ip instanceof SetterInjectionPoint) {
            return InjectionStrategy.SETTER;
        }
        else if(ip instanceof NoArgumentInjectionPoint) {
            return InjectionStrategy.NOARGUMENT;
        }
        else {
            return InjectionStrategy.DEFAULTCASE;
        }
    }
}
