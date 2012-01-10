package org.grouplens.inject.reflect;

import java.util.Comparator;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;

/**
 * ReflectionDesire is an abstract implementation of Desire.
 * 
 * @author Michael Ludwig
 */
abstract class ReflectionDesire implements Desire {
    private final AnnotationRole role;
    private final ReflectionSatisfaction satisfaction;

    /**
     * Create a ReflectionDesire that applies to the given role, and is
     * satisfied by the given satisfaction. The satisfaction can be null for a
     * non-instantiable desire. The role can be null to represent the default
     * role.
     * 
     * @param role The role of this desire, if there is one
     * @param satisfaction The satisfaction satisfying this desire, if there is
     *            one
     */
    public ReflectionDesire(AnnotationRole role, ReflectionSatisfaction satisfaction) {
        this.role = role;
        this.satisfaction = satisfaction;
    }

    /**
     * Return the type that is desired by this desire.
     * 
     * @return The desired type
     */
    public abstract Class<?> getDesiredType();
    
    @Override
    public boolean isParameter() {
        return (role != null ? role.isParameter() : false);
    }

    @Override
    public AnnotationRole getRole() {
        return role;
    }

    @Override
    public boolean isInstantiable() {
        return satisfaction != null;
    }

    @Override
    public ReflectionSatisfaction getSatisfaction() {
        return satisfaction;
    }

    @Override
    public Desire getDefaultDesire() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        // TODO Auto-generated method stub
        return null;
    }
}
