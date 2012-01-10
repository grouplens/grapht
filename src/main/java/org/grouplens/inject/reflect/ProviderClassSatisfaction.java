package org.grouplens.inject.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

class ProviderClassSatisfaction extends ReflectionSatisfaction {
    private final Class<? extends Provider<?>> providerType;
    
    public ProviderClassSatisfaction(Class<? extends Provider<?>> providerType) {
        if (providerType == null) {
            throw new NullPointerException("Provider class cannot be null");
        }
        
        this.providerType = providerType;
    }
    
    @Override
    public List<Desire> getDependencies() {
        return Types.getProviderDesires(providerType);
    }

    @Override
    public Type getType() {
        return Types.getProvidedType(providerType);
    }

    @Override
    public Class<?> getErasedType() {
        return Types.getProvidedType(providerType);
    }

    @Override
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        // FIXME: this injection code is largely independent from the Provider so we should move this
        //        to a common type. could this just be in a single Provider implementation that takes
        //        the desires and dependency function? I think so
        // FIXME: should the provider returned by this method be the actual provider created,
        //        or a wrapping provider that doesn't create the initial provider until the final
        //        object is first required?
        List<Desire> desires = getDependencies();
        
        // get the constructor required to create the provider instance
        Constructor<?> ctor = getConstructor(desires);
        
        // build up the constructor arguments from the dependencies function map
        Object[] ctorArgs = new Object[ctor.getParameterTypes().length];
        for (Desire d: desires) {
            if (d instanceof ConstructorParameterDesire) {
                ConstructorParameterDesire cd = (ConstructorParameterDesire) d;
                Provider<?> arg = dependencies.apply(d);
                if (arg == null) {
                    // FIXME: better exception, or should we just assume at this point?
                    throw new RuntimeException("Unable to complete dependencies");
                }
                
                ctorArgs[cd.getConstructorParameter()] = arg.get();
            }
        }
        
        // create the actual instance of the provider from the constructor
        Provider<?> provider;
        try {
            provider = (Provider<?>) ctor.newInstance(ctorArgs);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create Provider instance", e);
        }
        
        // inject any setter desires
        for (Desire d: desires) {
            if (d instanceof SetterMethodDesire) {
                SetterMethodDesire sd = (SetterMethodDesire) d;
                Provider<?> arg = dependencies.apply(d);
                if (arg == null) {
                    // FIXME: better exception, or should we just assume at this point?
                    throw new RuntimeException("Unable to complete dependencies");
                }
                
                try {
                    sd.getSetterMethod().invoke(provider, arg.get());
                } catch (Exception e) {
                    throw new RuntimeException("Unable to invoke setter injection method", e);
                }
            }
        }
        
        // the provider is completely configured so we can return it
        return provider;
    }
    
    private Constructor<?> getConstructor(List<Desire> dependencies) {
        for (Desire d: dependencies) {
            if (d instanceof ConstructorParameterDesire) {
                // since there is only one allowed constructor, all constructor desires
                // should have the same constructor
                return ((ConstructorParameterDesire) d).getConstructor();
            }
        }
        
        try {
            // FIXME: do we require that the default constructor be annotated with
            // @Inject as well?
            return providerType.getConstructor();
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
