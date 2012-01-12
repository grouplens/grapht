package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

import javax.inject.Provider;

import org.grouplens.inject.annotation.PassThrough;

/**
 * ConstructorParameterInjectionPoint is an injection point wrapping a parameter
 * of a constructor.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class ConstructorParameterInjectionPoint implements InjectionPoint {
    private final AnnotationRole role;
    private final Constructor<?> ctor;
    private final int parameter;
    private final boolean forProvider;

    /**
     * Create a ConstructorParameterInjectionPoint that wraps the given
     * parameter index for the given constructor, ctor.
     * 
     * @param ctor The constructor to wrap
     * @param parameter The parameter index of this injection point within
     *            ctor's parameters
     * @throws NullPointerException if ctor is null
     * @throws IndexOutOfBoundsException if parameter is not a valid index into
     *             the constructor's parameters
     */
    public ConstructorParameterInjectionPoint(Constructor<?> ctor, int parameter) {
        if (ctor == null) {
            throw new NullPointerException("Constructor cannot be null");
        }
        
        int numArgs = ctor.getParameterTypes().length;
        if (parameter < 0 || parameter >= numArgs) {
            throw new IndexOutOfBoundsException("Constructor parameter is invalid");
        }
        
        this.role = AnnotationRole.getRole(ctor.getParameterAnnotations()[parameter]);
        this.ctor = ctor;
        this.parameter = parameter;
        this.forProvider = Provider.class.isAssignableFrom(ctor.getDeclaringClass());
    }

    /**
     * @return The constructor wrapped by this injection point
     */
    public Constructor<?> getConstructor() {
        return ctor;
    }
    
    /**
     * @return The parameter index of this injection point within the
     *         constructor's parameters
     */
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
    public Class<?> getType() {
        return ctor.getParameterTypes()[parameter];
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConstructorParameterInjectionPoint)) {
            return false;
        }
        ConstructorParameterInjectionPoint cp = (ConstructorParameterInjectionPoint) o;
        return cp.ctor.equals(ctor) && cp.parameter == parameter;
    }
}
