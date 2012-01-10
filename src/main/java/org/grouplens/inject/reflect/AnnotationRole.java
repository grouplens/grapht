package org.grouplens.inject.reflect;

import java.lang.annotation.Annotation;

import org.grouplens.inject.annotation.DefaultBoolean;
import org.grouplens.inject.annotation.DefaultDouble;
import org.grouplens.inject.annotation.DefaultInt;
import org.grouplens.inject.annotation.DefaultString;
import org.grouplens.inject.annotation.DefaultType;
import org.grouplens.inject.annotation.InheritsDefaultRole;
import org.grouplens.inject.annotation.InheritsRole;
import org.grouplens.inject.annotation.Parameter;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Role;

class AnnotationRole implements Role {
    private final Class<? extends Annotation> roleType;
    
    public AnnotationRole(Class<? extends Annotation> roleType) {
        if (roleType == null)
            throw new NullPointerException("Role type cannot be null");
        if (!isRole(roleType))
            throw new IllegalArgumentException("Annotation is not a Role or Parameter annotation");
        this.roleType = roleType;
    }
    
    public static boolean isRole(Class<? extends Annotation> type) {
        return (type.getAnnotation(org.grouplens.inject.annotation.Role.class) != null ||
                type.getAnnotation(org.grouplens.inject.annotation.Parameter.class) != null);
    }
    
    public boolean isParameter() {
        return roleType.getAnnotation(Parameter.class) != null;
    }
    
    public Class<? extends Annotation> getRoleType() {
        return roleType;
    }
    
    public boolean inheritsRole() {
        return roleType.getAnnotation(InheritsRole.class) != null || roleType.getAnnotation(InheritsDefaultRole.class) != null;
    }
    
    public AnnotationRole getParentRole() {
        InheritsRole parentRole = roleType.getAnnotation(InheritsRole.class);
        if (parentRole != null) {
            return new AnnotationRole(parentRole.value());
        }
        
        // The parent role is still null if InheritsDefaultRole is on the
        // annotation, inheritsRole() distinguishes the cases.
        return null;
    }
    
    public BindRule getDefaultBinding() {
        if (isParameter()) {
            DefaultDouble dfltDouble = roleType.getAnnotation(DefaultDouble.class);
            if (dfltDouble != null) {
                // FIXME: return a parameter binding/instance binding
            }
            DefaultInt dfltInt = roleType.getAnnotation(DefaultInt.class);
            if (dfltInt != null) {
                // FIXME: return a ""
            }
            DefaultBoolean dfltBool = roleType.getAnnotation(DefaultBoolean.class);
            if (dfltBool != null) {
                // FIXME: return a ""
            }
            DefaultString dfltStr = roleType.getAnnotation(DefaultString.class);
            if (dfltStr != null) {
                // FIXME: return a ""
            }
        } else {
            DefaultType impl = roleType.getAnnotation(DefaultType.class);
            if (impl != null) {
                // FIXME: return a type bind rule
            }
        }
        
        // if there is no specified default, return a null binding
        return null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationRole)) {
            return false;
        }
        return ((AnnotationRole) o).roleType.equals(roleType);
    }
    
    @Override
    public int hashCode() {
        return roleType.hashCode();
    }
}
