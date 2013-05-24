package org.grouplens.grapht.annotation;

import java.lang.annotation.*;

/**
 * Mark a qualifier as allowing unqualified matches.  If a qualifier bears this annotation, then
 * the default qualifier matcher (what you get if you call {@link org.grouplens.grapht.Context#bind(Class)}
 * without calling any subsequent qualifier method) will match it.  This allows certain qualifiers
 * to fall-through to their unqualified implementations.  It also allows default implementations
 * to be used.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface AllowUnqualifiedMatch {
}
