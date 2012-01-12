package org.grouplens.inject.spi.reflect;

import java.util.Comparator;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;

/**
 * ReflectionDesire is an implementation of desire that contains all necessary
 * implementation to represent a desire, except that the point of injection is
 * abstracted by an {@link InjectionPoint}.
 * 
 * @author Michael Ludwig
 */
class ReflectionDesire implements Desire {
    private final Class<?> desiredType;
    private final InjectionPoint injectPoint;
    private final ReflectionSatisfaction satisfaction;

    /**
     * Create a ReflectionDesire that immediately wraps the given
     * InjectionPoint. The desired type equals the type declared by the
     * injection point. The created desire will have a satisfaction if the
     * injection point's type is satisfiable.
     * 
     * @param injectPoint The injection point to wrap
     * @throws NullPointerException if injectPoint is null
     */
    public ReflectionDesire(InjectionPoint injectPoint) {
        this(injectPoint.getType(), injectPoint, null);
    }
    
    /**
     * Create a ReflectionDesire that represents the dependency for
     * <tt>desiredType</tt> that will be injected into the given InjectionPoint.
     * The optional satisfaction will satisfy this desire. If null is provided,
     * the desired type is examined to see if it's already satisfiable.
     * 
     * @param desiredType The desired type of the dependency
     * @param injectPoint The injection point of the desire
     * @param satisfaction The satisfaction satisfying this desire, if there is
     *            one
     * @throws NullPointerException if desiredType or injectPoint is null
     * @throws IllegalArgumentException if desiredType is not assignable to the
     *             type of the injection point, or if the satisfaction's type is
     *             not assignable to the desired type
     */
    public ReflectionDesire(Class<?> desiredType, InjectionPoint injectPoint, ReflectionSatisfaction satisfaction) {
        if (desiredType == null || injectPoint == null) {
            throw new NullPointerException("Desired type and injection point cannot be null");
        }
        if (!injectPoint.getType().isAssignableFrom(desiredType) || 
            (satisfaction != null && !desiredType.isAssignableFrom(satisfaction.getErasedType()))) {
            throw new IllegalArgumentException("No type hierarchy between injection point, desired type, and satisfaction");
        }
        
        if (satisfaction == null) {
            satisfaction = Types.getSatisfaction(desiredType);
        }
        
        this.desiredType = desiredType;
        this.injectPoint = injectPoint;
        this.satisfaction = satisfaction;
    }

    /**
     * Return the type that is desired by this desire.
     * 
     * @return The desired type
     */
    public Class<?> getDesiredType() {
        return desiredType;
    }

    /**
     * Return the injection point used to inject whatever satisfies this desire.
     * 
     * @return The inject point for the desire
     */
    public InjectionPoint getInjectionPoint() {
        return injectPoint;
    }
    
    @Override
    public boolean isParameter() {
        AnnotationRole role = getRole();
        return (role != null ? role.isParameter() : false);
    }

    @Override
    public AnnotationRole getRole() {
        return injectPoint.getRole();
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
    public boolean isTransient() {
        return injectPoint.isTransient();
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
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionDesire)) {
            return false;
        }
        ReflectionDesire r = (ReflectionDesire) o;
        return (r.desiredType.equals(desiredType) && 
                r.injectPoint.equals(injectPoint) && 
                (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction)));
    }
}
