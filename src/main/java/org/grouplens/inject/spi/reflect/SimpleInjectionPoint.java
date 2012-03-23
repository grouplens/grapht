package org.grouplens.inject.spi.reflect;

import javax.annotation.Nullable;

import org.grouplens.inject.spi.Qualifier;

class SimpleInjectionPoint implements InjectionPoint {
    private final Qualifier qualifier;
    private final Class<?> type;
    
    public SimpleInjectionPoint(@Nullable Qualifier qualifier, Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Class type cannot be null");
        }
        this.qualifier = qualifier;
        this.type = type;
    }
    
    @Override
    public boolean isNullable() {
        return false;
    }
    
    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public Qualifier getQualifier() {
        return qualifier;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (qualifier == null ? 0 : qualifier.hashCode());
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleInjectionPoint)) {
            return false;
        }
        SimpleInjectionPoint p = (SimpleInjectionPoint) o;
        return p.type.equals(type) && (p.qualifier == null ? qualifier == null : p.qualifier.equals(qualifier));
    }
    
    @Override
    public String toString() {
        return "InjectionPoint(type=" + type + ", qualifier=" + qualifier + ")";
    }
}