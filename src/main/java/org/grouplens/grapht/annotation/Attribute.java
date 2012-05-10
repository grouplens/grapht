package org.grouplens.grapht.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Attribute is an auxiliary annotation, like {@link Qualifier}, that can be
 * used to add additional information to injection points (setters,
 * constructors, fields). Unlike qualifiers, attributes do not determine the
 * outcome of bindings, they only add information that is accessible in the
 * final dependency graph.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute { }
