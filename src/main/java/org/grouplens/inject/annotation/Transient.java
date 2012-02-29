package org.grouplens.inject.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.grouplens.inject.spi.Desire;

/**
 * The Transient annotation can be applied to injection points to flag the
 * dependency as 'transient'. Transient dependencies are only required in the
 * creation of the object and are not needed when using the instance.
 * 
 * @see Desire#isTransient()
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@Documented
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transient { }
