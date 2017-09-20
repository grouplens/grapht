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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.annotation.*;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.Qualifiers;
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
    public BindingResult bind(InjectionContext context, DesireChain dchain) throws ResolutionException {
        Desire desire = dchain.getCurrentDesire();
        BindingResult result = null;

        Annotation qualifier = desire.getInjectionPoint().getQualifier();

        // Only use qualifier defaults if this is the first desire 
        // (i.e. the desire that declared any qualifier)
        // REVIEW If it is not the first desire, can a qualifier exist?
        if (dchain.getPreviousDesires().isEmpty() && qualifier != null) {
            Class<? extends Annotation> annotType = qualifier.annotationType();
            annotType = Qualifiers.resolveAliases(annotType);

            result = getDefaultValue(desire, annotType);
            if (result == null) {
                result = getAnnotatedDefault(desire, annotType);
            }

            // if the qualifier does not allow fall-through, we're done
            if (!annotType.isAnnotationPresent(AllowDefaultMatch.class) && !annotType.isAnnotationPresent(AllowUnqualifiedMatch.class)) {
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
        BindingResult.Builder bld = null;
        DefaultDouble dfltDouble = type.getAnnotation(DefaultDouble.class);
        if (dfltDouble != null) {
            bld = BindingResult.newBuilder()
                               .setDesire(desire.restrict(Satisfactions.instance(dfltDouble.value())));
        }
        DefaultInteger dfltInt = type.getAnnotation(DefaultInteger.class);
        if (dfltInt != null) {
            bld = BindingResult.newBuilder().setDesire(desire.restrict(Satisfactions.instance(dfltInt.value())));
        }
        DefaultBoolean dfltBool = type.getAnnotation(DefaultBoolean.class);
        if (dfltBool != null) {
            bld = BindingResult.newBuilder().setDesire(desire.restrict(Satisfactions.instance(dfltBool.value())));
        }
        DefaultString dfltStr = type.getAnnotation(DefaultString.class);
        if (dfltStr != null) {
            bld = BindingResult.newBuilder().setDesire(desire.restrict(Satisfactions.instance(dfltStr.value())));
        }
        if (bld != null) {
            return bld.setCachePolicy(CachePolicy.NO_PREFERENCE)
                      .addFlag(BindingFlag.TERMINAL)
                      .build();
        } else {
            return null;
        }
    }

    /**
     * Get the default from annotations on the class, if present.
     *
     * @param type The type to scan for annotations.
     * @return A binding result, or {@code null} if no usable annotations are present.
     */
    private BindingResult getAnnotatedDefault(Desire desire, Class<?> type) {
        DefaultProvider provider = type.getAnnotation(DefaultProvider.class);
        BindingResult.Builder brb = null;
        if (provider != null) {
            brb = BindingResult.newBuilder()
                               .setDesire(desire.restrict(Satisfactions.providerType(provider.value())))
                               .setCachePolicy(provider.cachePolicy())
                               .addFlag(BindingFlag.TERMINAL);
            if (provider.skipIfUnusable()) {
                brb.addFlag(BindingFlag.SKIPPABLE);
            }
        }

        DefaultImplementation impl = type.getAnnotation(DefaultImplementation.class);
        if (impl != null) {
            brb = BindingResult.newBuilder()
                               .setCachePolicy(impl.cachePolicy());
            if (Types.isInstantiable(impl.value())) {
                brb.setDesire(desire.restrict(Satisfactions.type(impl.value())));
            } else {
                brb.setDesire(desire.restrict(impl.value()));
            }
            if (impl.skipIfUnusable()) {
                brb.addFlag(BindingFlag.SKIPPABLE);
            }
        }

        DefaultNull dnull = type.getAnnotation(DefaultNull.class);
        if (dnull != null) {
            brb = BindingResult.newBuilder()
                               .setDesire(desire.restrict(Satisfactions.nullOfType(desire.getDesiredType())))
                               .setCachePolicy(CachePolicy.NO_PREFERENCE)
                               .addFlag(BindingFlag.TERMINAL);
        }

        return brb != null ? brb.build() : null;
    }

    @SuppressWarnings("unchecked")
    private BindingResult getMetaInfDefault(Desire desire, Class<?> type) throws ResolutionException {
        synchronized (metaInfCache) {
            if (metaInfCache.containsKey(type)) {
                return metaInfCache.get(type);
            }
        }

        BindingResult.Builder builder = BindingResult.newBuilder();
        boolean found = false;
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
                throw new ResolutionException("error reading " + resourceName, e);
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
                    Satisfaction sat = Satisfactions.providerType((Class<Provider<?>>) clazz.asSubclass(Provider.class));
                    if (!type.isAssignableFrom(sat.getErasedType())) {
                        throw new ResolutionException(providerName + " does not provide " + type);
                    }
                    builder.setDesire(desire.restrict(sat))
                           .addFlag(BindingFlag.TERMINAL);
                    found = true;
                } catch (ClassNotFoundException e) {
                    throw new ResolutionException("cannot find default provider for " + type, e);
                }
            }

            String implName = props.getProperty("implementation");
            if (implName != null) {
                try {
                    logger.debug("found implementation {} for {}", implName, type);
                    Class<?> clazz = classLoader.loadClass(implName);
                    Satisfaction sat = Satisfactions.type(clazz);
                    if (!type.isAssignableFrom(sat.getErasedType())) {
                        throw new ResolutionException(providerName + " not compatible with " + type);
                    }
                    builder.setDesire(desire.restrict(sat));
                    found = true;
                } catch (ClassNotFoundException e) {
                    throw new ResolutionException("cannot find default implementation for " + type, e);
                }
            }

            String skip = props.getProperty("skipIfUnusable");
            if (skip != null && skip.trim().toLowerCase().equals("true")) {
                builder.addFlag(BindingFlag.SKIPPABLE);
            }

            if (found) {
                String policy = props.getProperty("cachePolicy", "NO_PREFERENCE");
                builder.setCachePolicy(CachePolicy.valueOf(policy));
            }
        }

        BindingResult result = found ? builder.build() : null;
        synchronized (metaInfCache) {
            metaInfCache.put(type, result);
        }
        return result;
    }
}
