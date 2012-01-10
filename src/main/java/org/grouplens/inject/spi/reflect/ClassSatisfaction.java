package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

class ClassSatisfaction extends ReflectionSatisfaction {
    private final Class<?> type;

    /**
     * Create a satisfaction wrapping the given class type.
     * 
     * @param type The type to wrap
     * @throws NullPointerException if type is null
     * @throws IllegalArgumentException if the type cannot be instantiated
     */
    public ClassSatisfaction(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Class type cannot be null");
        }
        if (!Types.isInstantiable(type)) {
            throw new IllegalArgumentException("Type cannot be instantiated");
        }
        this.type = type;
    }
    
    @Override
    public List<Desire> getDependencies() {
        return Types.getDesires(type);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return new InjectionProviderImpl(type, getDependencies(), dependencies);
    }
}
