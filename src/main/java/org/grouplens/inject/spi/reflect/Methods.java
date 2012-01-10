package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

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

    /**
     * Return the AnnotationRole representing the role contained in the
     * parameter annotations given. If the parameter annotations do not have any
     * annotation that is a role or parameter, then null is returned.
     * 
     * @param parameterAnnots The parameter annotations on the setter or
     *            constructor
     * @return The AnnotationRole for the injection point, or null if there is
     *         no role
     */
    public static AnnotationRole getRole(Annotation[] parameterAnnots) {
        for (int i = 0; i < parameterAnnots.length; i++) {
            if (AnnotationRole.isRole(parameterAnnots[i].annotationType())) {
                return new AnnotationRole(parameterAnnots[i].annotationType());
            }
        }
        return null;
    }

    /**
     * Return a satisfaction for the given type if it is satisfiable. A type is
     * satisfiable if it is an instantiable type, or if it has been annotated
     * with the {@link ProvidedBy} annotation. If the type cannot be satisfied,
     * null is returned. In this case, bind rules must be used to find a
     * satisfaction.
     * 
     * @param parameterType The type of parameter that will be satisfied byt the
     *            return satisfaction
     * @return A satisfaction for the given type, or null if it can't be on its
     *         own
     */
    public static ReflectionSatisfaction getSatisfaction(Class<?> parameterType) {
        ProvidedBy provider = parameterType.getAnnotation(ProvidedBy.class);
        if (provider != null) {
            // we have a provider type, so return a provider class satisfaction,
            // even if the desired type is an interface or abstract, we assume
            // the provider can be used successfully
            return new ProviderClassSatisfaction(provider.value());
        } else {
            // no provider is found, so we check if this is an instantiable class
            if (Types.isInstantiable(parameterType)) {
                return new ClassSatisfaction(parameterType);
            }
        }
        
        // no satisfaction is possible with the current type information
        return null;
    }
}
