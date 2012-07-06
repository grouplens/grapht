package org.grouplens.grapht.solver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceProvider;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * <p>
 * BindingFunction that enables provider-injection. This function supports
 * just-in-time binding for injection points for Providers, that creates
 * Providers wrapping whatever provided-type is necessary for that injection
 * point.
 * <p>
 * As an example, <code>Provider&lt;Foo&gt;</code> would have a Provider of Foo
 * injected, and the Foo instances returned by that Provider's get() method
 * would be configured as if the injection point was for <code>Foo</code>.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ProviderBindingFunction implements BindingFunction {
    private final InjectSPI spi;
    
    public ProviderBindingFunction(InjectSPI spi) {
        Preconditions.notNull("spi", spi);
        this.spi = spi;
    }
    
    @Override
    public BindingResult bind(InjectionContext context, Desire desire) throws SolverException {
        if (Provider.class.equals(desire.getDesiredType())) {
            // Look at the parameterized type of the injection point to
            // find what type of object should be provided
            Type providerType = desire.getInjectionPoint().getType();
            if (providerType instanceof ParameterizedType) {
                // Can only inject a Provider if it's a parameterized type,
                // otherwise we have no type information about the provided type
                Type[] typeArgs = ((ParameterizedType) providerType).getActualTypeArguments();
                if (typeArgs.length == 1 && (typeArgs[0] instanceof Class || 
                                             typeArgs[0] instanceof ParameterizedType)) {
                    Class<?> providedType = Types.erase(typeArgs[0]);
                    
                    // Create a desire for the provided type, cloning the attributes
                    // and nullability from the original desire
                    Desire providedDesire = spi.desire(desire.getInjectionPoint().getAttributes().getQualifier(), 
                                                       providedType, desire.getInjectionPoint().isNullable());
                    // Satisfied JIT desire for this injection point
                    Desire jitDesire = desire.restrict(new ProviderInjectionSatisfaction(providerType, providedDesire));
                    // Make sure to defer this binding since the single dependency
                    // on the provided type might very well create a cycle that deferred
                    // injection must break.
                    return new BindingResult(jitDesire, CachePolicy.NO_PREFERENCE, true, true);
                }
            }
        }
        
        // Not a Provider desire, or the type didn't have 
        // enough information to determine what we should provide
        return null;
    }

    /**
     * Satisfaction implementation that provides a Provider, and has a single
     * dependency on the provided type.
     */
    private static class ProviderInjectionSatisfaction implements Satisfaction {
        // FIXME: Must make this externalizable
        // FIXME: To do that easily, parameterizedType really should be transient
        // and get reconstructed from the providedDesire's type
        // FIXME That requires a ParameterizedTypeImpl that we'll have to bring back
        private final Type parameterizedType;
        private final Desire providedDesire;
        
        public ProviderInjectionSatisfaction(Type parameterizedType, Desire providedDesire) {
            this.parameterizedType = parameterizedType;
            this.providedDesire = providedDesire;
        }
        
        @Override
        public List<? extends Desire> getDependencies() {
            return Collections.singletonList(providedDesire);
        }

        @Override
        public Type getType() {
            return parameterizedType;
        }

        @Override
        public Class<?> getErasedType() {
            return Provider.class;
        }

        @Override
        public Provider<?> makeProvider(ProviderSource dependencies) {
            // Create a provider that provides another provider, since we'll
            // be injecting the provider and not the final object
            return new InstanceProvider<Provider<?>>(dependencies.apply(providedDesire));
        }

        @Override
        public Comparator<ContextMatcher> contextComparator() {
            // FIXME maybe this comparator logic really ought to be part
            // of the SPI and satisfaction. That would allow a lot more
            // composability
            return null;
        }
    }
}
