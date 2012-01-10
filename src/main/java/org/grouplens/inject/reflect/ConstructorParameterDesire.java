package org.grouplens.inject.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import org.grouplens.inject.annotation.PassThrough;

class ConstructorParameterDesire extends ReflectionDesire {
    private final Constructor<?> ctor;
    private final int parameter;
    private final boolean forProvider;
    
    public ConstructorParameterDesire(Constructor<?> ctor, int parameter, boolean forProvider) {
        super(Methods.getRole(ctor.getParameterAnnotations()[parameter]), 
              Methods.getSatisfaction(ctor.getParameterTypes()[parameter]));
        this.ctor = ctor;
        this.parameter = parameter;
        this.forProvider = forProvider;
    }

    public Constructor<?> getConstructor() {
        return ctor;
    }
    
    public int getConstructorParameter() {
        return parameter;
    }

    @Override
    public boolean isTransient() {
        boolean passThrough = false;
        Annotation[] annots = ctor.getParameterAnnotations()[parameter];
        for (int i = 0; i < annots.length; i++) {
            if (annots[i] instanceof PassThrough) {
                passThrough = true;
                break;
            }
        }
        
        // the desire is transient if it is for a provider and the parameter has
        // not been annotated as a pass-through dependency
        return forProvider && !passThrough;
    }

    @Override
    public Class<?> getDesiredType() {
        return ctor.getParameterTypes()[parameter];
    }
}
