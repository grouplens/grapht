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
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParameterizedType)) {
            return false;
        }
        ParameterizedType t = (ParameterizedType) o;
        return t.getRawType().equals(rawType) && 
               t.getOwnerType() == null &&
               Arrays.equals(arguments, t.getActualTypeArguments());
    }
    
    @Override
    public int hashCode() {
        return rawType.hashCode() ^ Arrays.hashCode(arguments);
    }
}
