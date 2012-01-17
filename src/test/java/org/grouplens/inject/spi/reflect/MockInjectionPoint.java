package org.grouplens.inject.spi.reflect;

import javax.annotation.Nullable;

/**
 * MockInjectionPoint is a simple injection point that wraps a type, role, and a
 * transient state. It has no actual injectable point but can be used when
 * constructing ReflectionDesires on the fly for tests.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockInjectionPoint implements InjectionPoint {
    private final Class<?> type;
    private final AnnotationRole role;
    private final boolean trans;
    
    public MockInjectionPoint(Class<?> type, @Nullable AnnotationRole role, boolean trans) {
        this.type = type;
        this.role = role;
        this.trans = trans;
    }
    
    
    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }

    @Override
    public boolean isTransient() {
        return trans;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MockInjectionPoint)) {
            return false;
        }
        MockInjectionPoint m = (MockInjectionPoint) o;
        return m.type.equals(type) && (m.role == null ? role == null : m.role.equals(role)) && m.trans == trans;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (role == null ? 0 : role.hashCode()) ^ (trans ? 1 : 0);
    }
}
