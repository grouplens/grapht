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
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
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
    private static final String META_INF_DEFAULTS = "/META-INF/grapht/defaults/";
    private final Logger logger = LoggerFactory.getLogger(DefaultDesireBindingFunction.class);
    private final InjectSPI spi;

    private final Map<Class<?>, BindingResult> metaInfCache =
            new HashMap<Class<?>, BindingResult>();
    
    public DefaultDesireBindingFunction(InjectSPI spi) {
        Preconditions.notNull("spi", spi);
        this.spi = spi;
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
            return new BindingResult(desire.restrict(spi.satisfy(dfltDouble.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        DefaultInteger dfltInt = type.getAnnotation(DefaultInteger.class);
        if (dfltInt != null) {
            return new BindingResult(desire.restrict(spi.satisfy(dfltInt.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        DefaultBoolean dfltBool = type.getAnnotation(DefaultBoolean.class);
        if (dfltBool != null) {
            return new BindingResult(desire.restrict(spi.satisfy(dfltBool.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }
        DefaultString dfltStr = type.getAnnotation(DefaultString.class);
        if (dfltStr != null) {
            return new BindingResult(desire.restrict(spi.satisfy(dfltStr.value())),
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
            return new BindingResult(desire.restrict(spi.satisfyWithProvider(provided.value())),
                                     CachePolicy.NO_PREFERENCE, false, true);
        }

        DefaultImplementation impl = type.getAnnotation(DefaultImplementation.class);
        if (impl != null) {
            if (Types.isInstantiable(impl.value())) {
                return new BindingResult(desire.restrict(spi.satisfy(impl.value())),
                                         CachePolicy.NO_PREFERENCE, false, false);
            } else {
                return new BindingResult(desire.restrict(impl.value()),
                                         CachePolicy.NO_PREFERENCE, false, false);
            }
        }

        DefaultNull dnull = type.getAnnotation(DefaultNull.class);
        if (dnull != null) {
            return new BindingResult(desire.restrict(spi.satisfyWithNull(desire.getDesiredType())),
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
        // FIXME Use a configurable class loader.
        InputStream istr = getClass().getResourceAsStream(resourceName);

        if (istr != null) {
            Properties props;
            try {
                props = new Properties();
                props.load(istr);
            } catch (IOException e) {
                throw new SolverException("error reading " + resourceName, e);
            } finally {
                try {
                    istr.close();
                } catch (IOException e) {
                    logger.error("error closing {}: {}", resourceName, e);
                }
            }

            String providerName = props.getProperty("provider");
            if (providerName != null) {
                try {
                    // FIXME Use the configured class loader
                    @SuppressWarnings("rawtypes")
                    Class providerClass = Class.forName(providerName);
                    logger.debug("found provider {} for {}", providerName, type);
                    // QUESTION: why should the last parameter be true?
                    result = new BindingResult(desire.restrict(spi.satisfyWithProvider(providerClass)),
                                               CachePolicy.NO_PREFERENCE, false, true);
                } catch (ClassNotFoundException e) {
                    throw new SolverException("cannot find default provider for " + type, e);
                }
            }

            String implName = props.getProperty("implementation");
            if (implName != null) {
                try {
                    // FIXME Use the configured class loader
                    @SuppressWarnings("rawtypes")
                    Class implClass = Class.forName(implName);
                    logger.debug("found implementation {} for {}", implName, type);
                    result = new BindingResult(desire.restrict(spi.satisfy(implClass)),
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
