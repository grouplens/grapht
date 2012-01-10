package org.grouplens.inject.reflect;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;

import com.google.common.base.Function;

class ClassSatisfaction extends ReflectionSatisfaction {
    private final Class<?> type;
    
    public ClassSatisfaction(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Class type cannot be null");
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
    @SuppressWarnings("rawtypes")
    public Provider<?> makeProvider(Function<? super Desire, ? extends Provider<?>> dependencies) {
        return new Provider() {
            @Override
            public Object get() {
                // TODO Auto-generated method stub
                return null;
            }
        };
    }
}
