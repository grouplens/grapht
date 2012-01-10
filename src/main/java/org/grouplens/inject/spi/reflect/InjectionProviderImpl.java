package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Constructor;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

/**
 * InjectionProviderImpl is a Provider implementation capable of creating any
 * type assuming that the type can be represented as a set of desires, and that
 * those desires are satisfied by other Provider implementations.
 * 
 * @author Michael Ludwig
 * @param <T> The object type that is provided
 */
// FIXME: we need better exceptions for dependency failures
public class InjectionProviderImpl<T> implements Provider<T> {
    private final Class<T> type;
    private final List<Desire> desires;
    private final Function<? super Desire, ? extends Provider<?>> providers;

    /**
     * Create an InjectionProviderImpl that will provide instances of the given
     * type, with given the list of desires and a function mapping that
     * satisfies those providers.
     * 
     * @param type The type of instance created
     * @param desires The dependency desires for the instance
     * @param providers The providers that satisfy the desires of the type
     */
    public InjectionProviderImpl(Class<T> type, List<Desire> desires, Function<? super Desire, ? extends Provider<?>> providers) {
        this.type = type;
        this.desires = desires;
        this.providers = providers;
    }

    @Override
    public T get() {
        // find constructor and build up necessary constructor arguments
        Constructor<T> ctor = getConstructor();
        Object[] args = new Object[ctor.getParameterTypes().length];
        for (Desire d: desires) {
            if (d instanceof ConstructorParameterDesire) {
                // this desire is a constructor argument so create it now
                ConstructorParameterDesire cd = (ConstructorParameterDesire) d;
                Provider<?> provider = providers.apply(d);
                if (provider == null) {
                    throw new RuntimeException("Unable to satisfy dependency");
                }
                
                args[cd.getConstructorParameter()] = provider.get();
            }
        }
        
        // create the instance that we are injecting
        T instance;
        try {
            instance = ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create instance", e);
        }
        
        // complete injection by satisfying any setter method dependencies
        for (Desire d: desires) {
            if (d instanceof SetterMethodDesire) {
                SetterMethodDesire sd = (SetterMethodDesire) d;
                Provider<?> provider = providers.apply(d);
                if (provider == null) {
                    throw new RuntimeException("Unable to satisfy dependency");
                }
                
                try {
                    sd.getSetterMethod().invoke(instance, provider.get());
                } catch (Exception e) {
                    throw new RuntimeException("Unable to inject setter dependency", e);
                }
            }
        }
        
        // the instance has been fully configured
        return instance;
    }
    
    @SuppressWarnings("unchecked")
    private Constructor<T> getConstructor() {
        for (Desire d: desires) {
            if (d instanceof ConstructorParameterDesire) {
                // since we only allow one injectable constructor, any ConstructorParameterDesire
                // will have the same constructor
                return (Constructor<T>) ((ConstructorParameterDesire) d).getConstructor();
            }
        }
        
        try {
            return type.getConstructor();
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("No default constructor for type: " + type, e);
        }
    }
}
