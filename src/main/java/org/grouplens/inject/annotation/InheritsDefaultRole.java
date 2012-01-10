package org.grouplens.inject.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InheritsDefaultRole is used to annotate role annotations to specify that they
 * inherit from the default or null role.
 * 
 * @see InheritsRole
 * @author Michael Ludwig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface InheritsDefaultRole { }
