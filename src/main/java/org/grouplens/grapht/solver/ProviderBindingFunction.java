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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.InstanceProvider;
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
                    Desire jitDesire = desire.restrict(new ProviderInjectionSatisfaction(providedDesire));
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
    private static class ProviderInjectionSatisfaction implements Satisfaction, Externalizable {
        private Desire providedDesire; // final
        
        public ProviderInjectionSatisfaction(Desire providedDesire) {
            this.providedDesire = providedDesire;
        }
        
        @Override
        public CachePolicy getDefaultCachePolicy() {
            return CachePolicy.NO_PREFERENCE;
        }
        
        @Override
        public List<? extends Desire> getDependencies() {
            return Collections.singletonList(providedDesire);
        }

        @Override
        public Type getType() {
            return Types.parameterizedType(Provider.class, providedDesire.getDesiredType());
        }

        @Override
        public Class<?> getErasedType() {
            return Provider.class;
        }

        @Override
        public boolean hasInstance() {
            return false;
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public Provider<?> makeProvider(ProviderSource dependencies) {
            Provider<?> trueProvider = dependencies.apply(providedDesire);
            
            // Return a provider wrapping this provider so the memoizing provider
            // is the final instance that is injected
            return new InstanceProvider<Provider<?>>(trueProvider);
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(providedDesire);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            providedDesire = (Desire) in.readObject();
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ProviderInjectionSatisfaction)) {
                return false;
            }
            
            return ((ProviderInjectionSatisfaction) o).providedDesire.equals(providedDesire);
        }
        
        @Override
        public int hashCode() {
            return providedDesire.hashCode();
        }
        
        @Override
        public String toString() {
            return "Provider<" + providedDesire + ">";
        }
    }
}
