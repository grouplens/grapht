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
        // FIXME: This needs to take into account -> default role bindings
        // and @ImplementedBy and @ProvidedBy annotations on the desired type
        return null;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        // FIXME: part of the bind-rule comparing will be whether or not it
        // is a generated rule.  It will not have to compare type distance
        // because it is assumed all bind rules will have a source type equaling
        // the desired type.
        
        // It will however have to determine role applicability ordering.
        // as well as generics specificity
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
