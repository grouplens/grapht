package org.grouplens.inject.spi.reflect;

import javax.annotation.Nullable;

import org.grouplens.inject.spi.Qualifier;

class SimpleInjectionPoint implements InjectionPoint {
    private final Qualifier qualifier;
    private final Class<?> type;
    private final boolean nullable;
    
    public SimpleInjectionPoint(@Nullable Qualifier qualifier, Class<?> type, boolean nullable) {
        if (type == null) {
            throw new NullPointerException("Class type cannot be null");
        }
        this.qualifier = qualifier;
        this.type = type;
        this.nullable = nullable;
    }
    
    @Override
    public boolean isNullable() {
        return nullable;
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
        return p.type.equals(type) && (p.qualifier == null ? qualifier == null : p.qualifier.equals(qualifier)) && p.nullable == nullable;
    }
    
    @Override
    public String toString() {
        String q = (qualifier == null ? "" : qualifier + ":");
        return q + type.getSimpleName();
    }
}