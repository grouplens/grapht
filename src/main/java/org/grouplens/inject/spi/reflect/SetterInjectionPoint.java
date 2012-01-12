package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Method;

import javax.inject.Provider;

import org.grouplens.inject.annotation.PassThrough;

/**
 * SetterInjectionPoint represents an injection point via a setter method.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class SetterInjectionPoint implements InjectionPoint {
    private final Method setter;
    private final AnnotationRole role;
    private final boolean forProvider;

    /**
     * Create a SetterInjectionPoint that wraps the given setter method.
     * 
     * @param setter The setter method
     */
    public SetterInjectionPoint(Method setter) {
        if (setter == null) {
            throw new NullPointerException("Setter method cannot null");
        }
        if (setter.getParameterTypes().length != 1) {
            throw new IllegalArgumentException("Setter must have a single parameter");
        }
        
        this.role = AnnotationRole.getRole(setter.getParameterAnnotations()[0]);
        this.setter = setter;
        this.forProvider = Provider.class.isAssignableFrom(setter.getDeclaringClass());
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
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
    public Class<?> getType() {
        return setter.getParameterTypes()[0];
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SetterInjectionPoint)) {
            return false;
        }
        return ((SetterInjectionPoint) o).setter.equals(setter);
    }
    
    @Override
    public int hashCode() {
        return setter.hashCode();
    }
}
