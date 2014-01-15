package org.grouplens.grapht.spi;

import org.grouplens.grapht.spi.reflect.ReflectionDesire;
import org.grouplens.grapht.spi.reflect.SimpleInjectionPoint;

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
        return new ReflectionDesire(new SimpleInjectionPoint(qualifier, type, nullable));
    }
}
