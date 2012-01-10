package org.grouplens.inject.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;

import org.grouplens.inject.annotation.ProvidedBy;

/**
 * Static helper methods for working with methods and constructors.
 * 
 * @review Should this be combined with Types into a more generic ReflectionUtil
 *         class?
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
final class Methods {
    private Methods() {}
    
    public static AnnotationRole getRole(Annotation[] parameterAnnots) {
        for (int i = 0; i < parameterAnnots.length; i++) {
            if (AnnotationRole.isRole(parameterAnnots[i].annotationType())) {
                return new AnnotationRole(parameterAnnots[i].annotationType());
            }
        }
        return null;
    }
    
    public static ReflectionSatisfaction getSatisfaction(Class<?> parameterType) {
        ProvidedBy provider = parameterType.getAnnotation(ProvidedBy.class);
        if (provider != null) {
            // we have a provider type, so return a provider class satisfaction,
            // even if the desired type is an interface or abstract, we assume
            // the provider can be used successfully
            return new ProviderClassSatisfaction(provider.value());
        } else {
            // no provider is found, so we check if this is an instantiable class
            if (!parameterType.isInterface() && !Modifier.isAbstract(parameterType.getModifiers())) {
                return new ClassSatisfaction(parameterType);
            }
        }
        
        // no satisfaction is possible with the current type information
        return null;
    }
}
