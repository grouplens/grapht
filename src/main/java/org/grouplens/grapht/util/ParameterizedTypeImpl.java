package org.grouplens.grapht.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

class ParameterizedTypeImpl implements ParameterizedType {
    private final Class<?> rawType;
    private final Type[] arguments;
    
    ParameterizedTypeImpl(Class<?> rawType, Type[] arguments) {
        this.rawType = rawType;
        this.arguments = arguments;
    }
    
    @Override
    public Type[] getActualTypeArguments() {
        // defensive copy
        return Arrays.copyOf(arguments, arguments.length);
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }
}
