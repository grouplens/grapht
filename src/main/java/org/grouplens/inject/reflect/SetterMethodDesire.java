package org.grouplens.inject.reflect;

import java.lang.reflect.Method;

import org.grouplens.inject.annotation.PassThrough;

class SetterMethodDesire extends ReflectionDesire {
    private final Method setter;
    private final boolean forProvider;
    
    public SetterMethodDesire(Method setter, boolean forProvider) {
        super(Methods.getRole(setter.getParameterAnnotations()[0]),
              Methods.getSatisfaction(setter.getParameterTypes()[0]));
        this.setter = setter;
        this.forProvider = forProvider;
    }
    
    public Method getSetterMethod() {
        return setter;
    }
    
    @Override
    public boolean isTransient() {
        // the desire is transient if it's a dependency for a provider but 
        // is not a pass-through dependency (i.e. it's not used by the provided
        // object after creation).
        return forProvider && setter.getAnnotation(PassThrough.class) == null;
    }

    @Override
    public Class<?> getDesiredType() {
        return setter.getParameterTypes()[0];
    }
}
