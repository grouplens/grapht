package org.grouplens.inject.spi.reflect;

import org.grouplens.inject.spi.Desire;

/**
 * InjectionPoint represents a point of injection for an instantiable type.
 * Examples include a constructor parameter, a setter method, or a field.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
interface InjectionPoint {
    /**
     * Return the type required to satisfy the injection point.
     * 
     * @return The type of the injection point
     */
    Class<?> getType();

    /**
     * Return any role on this injection point, or null if it is the default
     * role.
     * 
     * @return The role on the injection point
     */
    AnnotationRole getRole();

    /**
     * Return whether or not this injection point is a transient, with the same
     * definition of {@link Desire#isTransient()}.
     * 
     * @return True if the injection point is for a transient desire
     */
    boolean isTransient();
}
