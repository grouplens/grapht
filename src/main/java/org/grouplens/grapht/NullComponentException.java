package org.grouplens.grapht;

import org.grouplens.grapht.reflect.InjectionPoint;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class NullComponentException extends InjectionException {
    private final InjectionPoint injectionPoint;

    public NullComponentException(InjectionPoint point) {
        super(point.getMember().getDeclaringClass(), point.getMember());
        injectionPoint = point;
    }

    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }

    public String getMessage() {
        return "No component available for non-nullable injection point " + injectionPoint;
    }
}
