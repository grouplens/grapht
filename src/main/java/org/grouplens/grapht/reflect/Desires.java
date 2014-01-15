package org.grouplens.grapht.reflect;

import org.grouplens.grapht.reflect.internal.AttributesImpl;
import org.grouplens.grapht.reflect.internal.ReflectionDesire;
import org.grouplens.grapht.reflect.internal.SimpleInjectionPoint;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Desires {
    private Desires() {}

    /**
     * Create a new desire.
     * @param qualifier The qualifier applied to the type.
     * @param type The desired type.
     * @param nullable Whether this injection is nullable.
     * @return The desire.
     */
    public static Desire create(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        return new ReflectionDesire(createInjectionPoint(qualifier, type, nullable));
    }

    public static InjectionPoint createInjectionPoint(@Nullable Annotation qualifier, Class<?> type, boolean nullable) {
        return new SimpleInjectionPoint(qualifier, type, nullable);
    }

    public static AttributesImpl createAttributes(Annotation... annots) {
        return new AttributesImpl(annots);
    }
}
