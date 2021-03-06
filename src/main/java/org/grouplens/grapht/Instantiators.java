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
package org.grouplens.grapht;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.grouplens.grapht.util.LogContext;
import org.grouplens.grapht.util.TypedProvider;
import org.grouplens.grapht.util.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;

/**
 * Utilities and methods for building and working with {@link org.grouplens.grapht.Instantiator}s.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.9
 */
public final class Instantiators {
    private static final Logger logger = LoggerFactory.getLogger(Instantiators.class);

    private Instantiators() {}

    /**
     * Create an instantiator that returns an instance.
     * @param inst The instance to return (must be non-null).
     * @return An instantiator that returns {@code inst}.
     */
    public static Instantiator ofInstance(Object inst) {
        Preconditions.checkNotNull(inst, "instance");
        return new InstanceInstantiator(inst);
    }

    /**
     * Create an instantiator that returns a null value.
     * @param type The type of null value to return.
     * @return An instantiator that returns {@code null}.
     */
    public static Instantiator ofNull(Class<?> type) {
        Preconditions.checkNotNull(type, "instance");
        return new InstanceInstantiator(null, type);
    }

    /**
     * Convert a providerInstantiator to an instantiator.  Any exception thrown by the provider - including a
     * runtime exception - is wrapped in an {@link ConstructionException}.
     * @param provider The providerInstantiator to wrap.
     * @return An instantiator wrapping {@code providerInstantiator}.
     */
    public static Instantiator ofProvider(Provider<?> provider) {
        Preconditions.checkNotNull(provider, "provider");
        return new ProviderInstantiator(ofInstance(provider));
    }

    /**
     * Flatten an instnatiator of providers into an instantiator of the provided type.  Any
     * exception thrown by the provider - including a runtime exception - is wrapped in an
     * {@link ConstructionException}.
     * @param pinst The providerInstantiator instantiator to wrap.
     * @return An instantiator wrapping {@code providerInstantiator}.
     */
    public static Instantiator ofProviderInstantiator(Instantiator pinst) {
        Preconditions.checkNotNull(pinst, "provider instantiator");
        Preconditions.checkArgument(Provider.class.isAssignableFrom(pinst.getType()),
                                    "instantiator is not of type Provider");

        return new ProviderInstantiator(pinst);
    }

    /**
     * Convert an instantiator to a provider.
     * @param instantiator The instantiator to convert.
     * @return A provider whose {@link javax.inject.Provider#get()} method invokes the instantiator.
     */
    public static Provider<?> toProvider(Instantiator instantiator) {
        // First try to unpack the instantiator
        if (instantiator instanceof ProviderInstantiator) {
            Instantiator itor = ((ProviderInstantiator) instantiator).providerInstantiator;
            if (itor instanceof InstanceInstantiator) {
                return (Provider) ((InstanceInstantiator) itor).instance;
            }
        }
        // Otherwise, wrap it.

        return new InstantiatorProvider(instantiator);
    }

    /**
     * Memoize an instantiator.
     * @param instantiator The instantiator to memoize.
     * @return An instantiator that memoizes {@code instantiator}.
     */
    public static Instantiator memoize(Instantiator instantiator) {
        Preconditions.checkNotNull(instantiator, "instantiator");
        return new MemoizingInstantiator(instantiator);
    }
    private static final class InstanceInstantiator implements Instantiator {
        private final Object instance;
        private final Class<?> type;

        public InstanceInstantiator(Object inst) {
            this(inst, (Class) inst.getClass());
        }

        public InstanceInstantiator(Object inst, Class<?> typ) {
            instance = inst;
            type = typ;
        }

        @Override
        public Object instantiate() throws ConstructionException {
            return instance;
        }

        @Override
        public Class getType() {
            return type;
        }
    }

    private static class ProviderInstantiator implements Instantiator {
        private final Instantiator providerInstantiator;
        public ProviderInstantiator(Instantiator prov) {
            providerInstantiator = prov;
        }

        @Override
        public Object instantiate() throws ConstructionException {
            Provider<?> provider = (Provider) providerInstantiator.instantiate();
            logger.trace("invoking provider {}", provider);
            try (LogContext mdcContextProvider = LogContext.create()) {
                mdcContextProvider.put("org.grouplens.grapht.currentProvider", provider.toString());
                return provider.get();
            } catch (Exception th) {
                throw new ConstructionException(getType(), "Error invoking provider " + providerInstantiator, th);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<?> getType() {
            return Types.getProvidedType(providerInstantiator.getType());
        }
    }


    private static class MemoizingInstantiator implements Instantiator {
        private final Instantiator delegate;
        private volatile boolean instantiated = false;
        private Object instance = null;
        private Throwable error = null;

        public MemoizingInstantiator(Instantiator inst) {
            delegate = inst;
        }

        @Override
        public Object instantiate() throws ConstructionException {
            if (!instantiated) {
                synchronized (this) {
                    if (!instantiated) {
                        try {
                            instance = delegate.instantiate();
                        } catch (Exception th) {
                            error = th;
                        }
                        instantiated = true;
                    }
                }
            }

            if (error != null) {
                Throwables.propagateIfPossible(error, ConstructionException.class);
                // shouldn't happen, but hey.
                throw Throwables.propagate(error);
            } else {
                return instance;
            }
        }

        @Override
        public Class getType() {
            return delegate.getType();
        }
    }

    private static class InstantiatorProvider implements TypedProvider {
        private final Instantiator instantiator;

        public InstantiatorProvider(Instantiator itor) {
            instantiator = itor;
        }

        @Override
        public Class<?> getProvidedType() {
            return instantiator.getType();
        }

        @Override
        public Object get() {
            try {
                logger.trace("invoking instantiator {}", instantiator);
                return getProvidedType().cast(instantiator.instantiate());
            } catch (ConstructionException ex) {
                throw new UncheckedExecutionException(ex);
            }
        }
    }

}
