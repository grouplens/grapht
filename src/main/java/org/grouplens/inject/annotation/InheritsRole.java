package org.grouplens.inject.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * InheritsRole can be used to annotate an existing role annotation so that it
 * inherits from another role. When a role inherits from another, it will match
 * any context or role requirement that matches the parent role. Essentially, it
 * is identical to inheriting from a class type.
 * 
 * @author Michael Ludwig
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface InheritsRole {
    Class<? extends Annotation> value();
}
