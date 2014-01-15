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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.annotation.*;
import org.grouplens.grapht.reflect.CachePolicy;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.Satisfactions;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A binding function that looks for {@link DefaultImplementation} or
 * {@link DefaultProvider} on the desired type or the qualifier. For constants,
 * it will also check for {@link DefaultDouble}, {@link DefaultInteger},
 * {@link DefaultBoolean}, and {@link DefaultString}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class DefaultDesireBindingFunction implements BindingFunction {
    private static final String META_INF_DEFAULTS = "META-INF/grapht/defaults/";
    private final Logger logger = LoggerFactory.getLogger(DefaultDesireBindingFunction.class);
    private final ClassLoader classLoader;

    private final Map<Class<?>, BindingResult> metaInfCache =
            new HashMap<Class<?>, BindingResult>();
    
    DefaultDesireBindingFunction(ClassLoader loader) {
        Preconditions.notNull("spi", loader);
        classLoader = loader;
    }

    public static DefaultDesireBindingFunction create(ClassLoader loader) {
        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }
        if (loader == null) {
            loader = DefaultDesireBindingFunction.class.getClassLoader();
        }
        return new DefaultDesireBindingFunction(loader);
    }

    public static DefaultDesireBindingFunction create() {
        return create(null);
    }
    
    @Override
    public BindingResult bind(InjectionContext context, DesireChain dchain) throws SolverException {
        Desire desire = dchain.getCurrentDesire();
        BindingResult result = null;

        Annotation qualifier = desire.getInjectionPoint().getAttributes().getQualifier();

        // Only use qualifier defaults if this is the first desire 
        // (i.e. the desire that declared any qualifier)
        // REVIEW If it is not the first desire, can a qualifier exist?
        if (dchain.getPreviousDesires().isEmpty() && qualifier != null) {
            Class<? extends Annotation> annotType = qualifier.annotationType();

            result = getDefaultValue(desire, annotType);
            if (result == null) {
                result = getAnnotatedDefault(desire, annotType);
            }

            // if the qualifier does not allow fall-through, we're done
            if (!qualifier.annotationType().isAnnotationPresent(AllowUnqualifiedMatch.class)) {
                return result;
            }
        }

        // Now check the desired type for @DefaultImplementation or @DefaultProvider if the type
        // source has not been disabled.
        if (result == null) {
            result = getAnnotatedDefault(desire, desire.getDesiredType());
        }

        // Last-ditch, try to get a default from META-INF
        if (result == null) {
            result = getMetaInfDefault(desire, desire.getDesiredType());
        }
        
        // There are no annotations on the {@link Qualifier} or the type that indicate a
        // default binding or value, or the defaults have been disabled,
        // so we return null
        return result;
    }

    /**
     * Get a default value (double, integer, string, etc.).
     * @param desire The desire to satisfy.
     * @param type The class to scan for annotations.
     * @return The binding result, or {@code null} if there are no relevant annotations.
     */
    private BindingResult getDefaultValue(Desire desire, Class<?> type) {
        // FIXME Check whether the annotation type is actually relevant for the desire
        DefaultDouble dfltDouble = type.getAnnotation(DefaultDouble.class);
        if (dfltDouble != null) {
            return new BindingResult(desire.restrict(Satisfactions.satisfy(dfltDouble.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        DefaultInteger dfltInt = type.getAnnotation(DefaultInteger.class);
        if (dfltInt != null) {
            return new BindingResult(desire.restrict(Satisfactions.satisfy(dfltInt.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        DefaultBoolean dfltBool = type.getAnnotation(DefaultBoolean.class);
        if (dfltBool != null) {
            return new BindingResult(desire.restrict(Satisfactions.satisfy(dfltBool.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        DefaultString dfltStr = type.getAnnotation(DefaultString.class);
        if (dfltStr != null) {
            return new BindingResult(desire.restrict(Satisfactions.satisfy(dfltStr.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        return null;
    }

    /**
     * Get the default from annotations on the class, if present.
     *
     * @param type The type to scan for annotations.
     * @return A binding result, or {@code null} if no usable annotations are present.
     */
    private BindingResult getAnnotatedDefault(Desire desire, Class<?> type) {
        DefaultProvider provided = type.getAnnotation(DefaultProvider.class);
        if (provided != null) {
            return new BindingResult(desire.restrict(Satisfactions.satisfyWithProvider(provided.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }

        DefaultImplementation impl = type.getAnnotation(DefaultImplementation.class);
        if (impl != null) {
            if (Types.isInstantiable(impl.value())) {
                return new BindingResult(desire.restrict(Satisfactions.satisfy(impl.value())),
                                         CachePolicy.NO_PREFERENCE, false, false);
            } else {
                return new BindingResult(desire.restrict(impl.value()),
                                         CachePolicy.NO_PREFERENCE, false, false);
            }
        }

        DefaultNull dnull = type.getAnnotation(DefaultNull.class);
        if (dnull != null) {
            return new BindingResult(desire.restrict(Satisfactions.satisfyWithNull(desire.getDesiredType())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private BindingResult getMetaInfDefault(Desire desire, Class<?> type) throws SolverException {
        synchronized (metaInfCache) {
            if (metaInfCache.containsKey(type)) {
                return metaInfCache.get(type);
            }
        }

        BindingResult result = null;
        String resourceName = META_INF_DEFAULTS + type.getCanonicalName() + ".properties";
        logger.debug("searching for defaults in {}", resourceName);
        URL url = classLoader.getResource(resourceName);

        if (url != null) {
            Properties props;
            InputStream istr = null;
            try {
                istr = url.openStream();
                props = new Properties();
                props.load(istr);
            } catch (IOException e) {
                throw new SolverException("error reading " + resourceName, e);
            } finally {
                try {
                    if (istr != null) {
                        istr.close();
                    }
                } catch (IOException e) {
                    logger.error("error closing {}: {}", resourceName, e);
                }
            }

            String providerName = props.getProperty("provider");
            if (providerName != null) {
                try {
                    logger.debug("found provider {} for {}", providerName, type);
                    Class<?> clazz = classLoader.loadClass(providerName);
                    Satisfaction sat = Satisfactions.satisfyWithProvider((Class<Provider<?>>) clazz.asSubclass(Provider.class));
                    if (!type.isAssignableFrom(sat.getErasedType())) {
                        throw new SolverException(providerName + " does not provide " + type);
                    }
                    // QUESTION: why should the last parameter be true?
                    result = new BindingResult(desire.restrict(sat),
                                               CachePolicy.NO_PREFERENCE, false, true);
                } catch (ClassNotFoundException e) {
                    throw new SolverException("cannot find default provider for " + type, e);
                }
            }

            String implName = props.getProperty("implementation");
            if (implName != null) {
                try {
                    logger.debug("found implementation {} for {}", implName, type);
                    Class<?> clazz = classLoader.loadClass(implName);
                    Satisfaction sat = Satisfactions.satisfy(clazz);
                    if (!type.isAssignableFrom(sat.getErasedType())) {
                        throw new SolverException(providerName + " not compatible with " + type);
                    }
                    result = new BindingResult(desire.restrict(sat),
                                               CachePolicy.NO_PREFERENCE, false, false);
                } catch (ClassNotFoundException e) {
                    throw new SolverException("cannot find default implementation for " + type, e);
                }
            }
        }
        synchronized (metaInfCache) {
            metaInfCache.put(type, result);
        }
        return result;
    }
}
