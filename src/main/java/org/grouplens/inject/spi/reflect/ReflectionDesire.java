/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.grouplens.inject.spi.reflect;

import java.util.Comparator;

import org.grouplens.inject.annotation.DefaultBoolean;
import org.grouplens.inject.annotation.DefaultDouble;
import org.grouplens.inject.annotation.DefaultInt;
import org.grouplens.inject.annotation.DefaultString;
import org.grouplens.inject.annotation.DefaultType;
import org.grouplens.inject.annotation.ImplementedBy;
import org.grouplens.inject.annotation.ProvidedBy;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;

/**
 * ReflectionDesire is an implementation of desire that contains all necessary
 * implementation to represent a desire, except that the point of injection is
 * abstracted by an {@link InjectionPoint}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionDesire implements Desire {
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
     * and the desired type instantiable, a ClassSatisfaction is created.
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
        
        desiredType = Types.box(desiredType);
        if (!injectPoint.getType().isAssignableFrom(desiredType) || 
            (satisfaction != null && !desiredType.isAssignableFrom(satisfaction.getErasedType()))) {
            throw new IllegalArgumentException("No type hierarchy between injection point, desired type, and satisfaction");
        }
        
        if (satisfaction == null && Types.isInstantiable(desiredType)) {
            satisfaction = new ClassSatisfaction(desiredType);
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
        // First we check the role if it has a default binding
        AnnotationRole role = getRole();
        while(role != null) {
            if (role.isParameter()) {
                DefaultDouble dfltDouble = role.getRoleType().getAnnotation(DefaultDouble.class);
                if (dfltDouble != null) {
                    return new InstanceBindRule(dfltDouble.value(), Double.class, role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
                }
                DefaultInt dfltInt = role.getRoleType().getAnnotation(DefaultInt.class);
                if (dfltInt != null) {
                    return new InstanceBindRule(dfltInt.value(), Integer.class, role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
                }
                DefaultBoolean dfltBool = role.getRoleType().getAnnotation(DefaultBoolean.class);
                if (dfltBool != null) {
                    return new InstanceBindRule(dfltBool.value(), Boolean.class, role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
                }
                DefaultString dfltStr = role.getRoleType().getAnnotation(DefaultString.class);
                if (dfltStr != null) {
                    return new InstanceBindRule(dfltStr.value(), String.class, role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
                }
            } else {
                DefaultType impl = role.getRoleType().getAnnotation(DefaultType.class);
                if (impl != null) {
                    return new ClassBindRule(impl.value(), getDesiredType(), role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
                }
            }
            
            // there was no default binding on the role, so check its parent role
            role = (role.inheritsRole() ? role.getParentRole() : null);
        }
        
        // Now check the desired type for @ImplementedBy or @ProvidedBy
        ProvidedBy provided = getDesiredType().getAnnotation(ProvidedBy.class);
        if (provided != null) {
            return new ProviderClassBindRule(provided.value(), getDesiredType(), role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
        }
        ImplementedBy impl = getDesiredType().getAnnotation(ImplementedBy.class);
        if (impl != null) {
            return new ClassBindRule(impl.value(), getDesiredType(), role, BindRule.SECOND_TIER_GENERATED_BIND_RULE).apply(this);
        }
        
        // There are no annotations on the role or the type that indicate a default binding or value
        // so we return null
        return null;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        // 1st comparison is manual vs generated bind rules
        //  - manual bind rules are preferred over any generated rules
        // 2nd comparison is how close the desire's role is to the bind rules
        //  - we know that the desire's is a sub-role of any bind rules, so choose
        //    the bind rule with the closest distance
        // 3rd comparison is how well the generics match
        //  - for now, we don't track generic types so it is ignored
        return new Comparator<BindRule>() {
            @Override
            public int compare(BindRule o1, BindRule o2) {
                ReflectionBindRule b1 = (ReflectionBindRule) o1;
                ReflectionBindRule b2 = (ReflectionBindRule) o2;
                
                // #1 - select manual over generated
                if (b1.getWeight() != b2.getWeight()) {
                    return b1.getWeight() - b2.getWeight();
                }
                
                // #2 - select shorter role distance
                int d1 = AnnotationRole.getRoleDistance(ReflectionDesire.this.getRole(), b1.getRole());
                int d2 = AnnotationRole.getRoleDistance(ReflectionDesire.this.getRole(), b2.getRole());
                if (d1 != d2) {
                    return d1 - d2;
                }

                // #3 - generics specificity TODO
                return 0;
            }
        };
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
