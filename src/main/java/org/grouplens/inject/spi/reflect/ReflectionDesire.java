/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import org.grouplens.inject.annotation.DefaultBoolean;
import org.grouplens.inject.annotation.DefaultDouble;
import org.grouplens.inject.annotation.DefaultImplementation;
import org.grouplens.inject.annotation.DefaultInteger;
import org.grouplens.inject.annotation.DefaultProvider;
import org.grouplens.inject.annotation.DefaultString;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.util.Types;

/**
 * ReflectionDesire is an implementation of desire that contains all necessary
 * implementation to represent a desire, except that the point of injection is
 * abstracted by an {@link InjectionPoint}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionDesire implements Desire {
    /**
     * Return a list of desires that must satisfied in order to instantiate the
     * given type.
     *
     * @param type The class type whose dependencies will be queried
     * @return The dependency desires for the given type
     * @throws NullPointerException if the type is null
     */
    public static List<ReflectionDesire> getDesires(Class<?> type) {
        List<ReflectionDesire> desires = new ArrayList<ReflectionDesire>();

        boolean ctorFound = false;
        for (Constructor<?> ctor: type.getConstructors()) {
            if (ctor.getAnnotation(Inject.class) != null) {
                if (!ctorFound) {
                    ctorFound = true;
                    for (int i = 0; i < ctor.getParameterTypes().length; i++) {
                        desires.add(new ReflectionDesire(new ConstructorParameterInjectionPoint(ctor, i)));
                    }
                } else {
                    // at the moment there can only be one injectable constructor
                    // FIXME: return a better exception with more information
                    throw new RuntimeException("Too many injectable constructors");
                }
            }
        }

        for (Method m: type.getMethods()) {
            if (m.getAnnotation(Inject.class) != null) {
                for (int i = 0; i < m.getParameterTypes().length; i++) {
                    desires.add(new ReflectionDesire(new SetterInjectionPoint(m, i)));
                }
            }
        }

        return Collections.unmodifiableList(desires);
    }

    // FIXME: review the requirements for this
    public static enum DefaultSource {
        /**
         * Neither type nor qualifier will be examined for a default desire.
         */
        NONE,
        /**
         * Only the desired type is examined for a default desire.
         */
        TYPE,
        /**
         * Only the qualifier of the desire (or its injection point) is examined for
         * a default desire.
         */
        QUALIFIER,
        /**
         * Both type and qualifier are examined for defaults, prioritizing qualifier over
         * type. This is the default.
         */
        QUALIFIER_AND_TYPE
    }
    
    private final Class<?> desiredType;
    private final InjectionPoint injectPoint;
    private final ReflectionSatisfaction satisfaction;

    private final DefaultSource dfltSource;

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
        this(injectPoint.getType(), injectPoint, null, DefaultSource.QUALIFIER_AND_TYPE);
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
     *            @param dfltSource The source of default bindings for this desire
     * @throws NullPointerException if desiredType, injectPoint, or dfltSource is null
     * @throws IllegalArgumentException if desiredType is not assignable to the
     *             type of the injection point, or if the satisfaction's type is
     *             not assignable to the desired type
     */
    public ReflectionDesire(Class<?> desiredType, InjectionPoint injectPoint,
                            ReflectionSatisfaction satisfaction, DefaultSource dfltSource) {
        if (desiredType == null || injectPoint == null || dfltSource == null) {
            throw new NullPointerException("Desired type, injection point, and default source cannot be null");
        }

        desiredType = Types.box(desiredType);
        if (!injectPoint.getType().isAssignableFrom(desiredType) || (satisfaction != null && !desiredType.isAssignableFrom(satisfaction.getErasedType()))) {
            throw new IllegalArgumentException("No type hierarchy between injection point, desired type, and satisfaction");
        }

        // try and find a satisfaction
        if (satisfaction == null) {
            if (Types.isInstantiable(desiredType)) {
                satisfaction = new ClassSatisfaction(desiredType);
            } else if (injectPoint.isNullable()) {
                satisfaction = new NullSatisfaction(desiredType);
            }
        }

        this.desiredType = desiredType;
        this.injectPoint = injectPoint;
        this.satisfaction = satisfaction;
        this.dfltSource = dfltSource;
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
    public Qualifier getQualifier() {
        return injectPoint.getQualifier();
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
        Qualifier qualifier = getQualifier();

        // Check the qualifier first, but only if the qualifier source hasn't been disabled
        if (dfltSource == DefaultSource.QUALIFIER || dfltSource == DefaultSource.QUALIFIER_AND_TYPE) {
            while (qualifier != null) {
                DefaultDouble dfltDouble = qualifier.getAnnotation(DefaultDouble.class);
                if (dfltDouble != null) {
                    return new ReflectionDesire(Double.class, injectPoint, new InstanceSatisfaction(dfltDouble.value()), DefaultSource.TYPE);
                }
                DefaultInteger dfltInt = qualifier.getAnnotation(DefaultInteger.class);
                if (dfltInt != null) {
                    return new ReflectionDesire(Integer.class, injectPoint, new InstanceSatisfaction(dfltInt.value()), DefaultSource.TYPE);
                }
                DefaultBoolean dfltBool = qualifier.getAnnotation(DefaultBoolean.class);
                if (dfltBool != null) {
                    return new ReflectionDesire(Boolean.class, injectPoint, new InstanceSatisfaction(dfltBool.value()), DefaultSource.TYPE);
                }
                DefaultString dfltStr = qualifier.getAnnotation(DefaultString.class);
                if (dfltStr != null) {
                    return new ReflectionDesire(String.class, injectPoint, new InstanceSatisfaction(dfltStr.value()), DefaultSource.TYPE);
                }
                DefaultImplementation impl = qualifier.getAnnotation(DefaultImplementation.class);
                if (impl != null) {
                    // let the constructor create any satisfaction
                    return new ReflectionDesire(impl.value(), injectPoint, null, DefaultSource.TYPE);
                }
                
                // there was no default binding on the qualifier, 
                // so check its parent qualifier
                qualifier = qualifier.getParent();
            }
        }
        
        // Now check the desired type for @DefaultImplementation or @DefaultProvider if the type
        // source has not been disabled.
        if (dfltSource == DefaultSource.TYPE || dfltSource == DefaultSource.QUALIFIER_AND_TYPE) {
            DefaultProvider provided = getDesiredType().getAnnotation(DefaultProvider.class);
            if (provided != null) {
                return new ReflectionDesire(Types.getProvidedType(provided.value()), injectPoint, 
                                            new ProviderClassSatisfaction(provided.value()), DefaultSource.TYPE);
            }
            DefaultImplementation impl = getDesiredType().getAnnotation(DefaultImplementation.class);
            if (impl != null) {
                // let the constructor create any satisfaction
                return new ReflectionDesire(impl.value(), injectPoint, null, DefaultSource.TYPE);
            }
        }
        
        // There are no annotations on the {@link Qualifier} or the type that indicate a
        // default binding or value, or the defaults have been disabled,
        // so we return null
        return null;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        // 1st comparison is manual vs generated bind rules
        // - manual bind rules are preferred over any generated rules
        // 2nd comparison is how close the desire's {@link Qualifier} is to the bind rules
        // - we know that the desire's is a sub-{@link Qualifier} of any bind rules, so
        // choose
        // the bind rule with the closest distance
        // 3rd comparison is how well the generics match
        // - for now, we don't track generic types so it is ignored
        return new Comparator<BindRule>() {
            @Override
            public int compare(BindRule o1, BindRule o2) {
                ReflectionBindRule b1 = (ReflectionBindRule) o1;
                ReflectionBindRule b2 = (ReflectionBindRule) o2;

                // #1 - select manual over generated
                if (b1.getWeight() != b2.getWeight()) {
                    return b1.getWeight() - b2.getWeight();
                }

                // #2 - select shorter qualifier distance
                int d1 = Qualifiers.getQualifierDistance(ReflectionDesire.this.getQualifier(),
                                                         b1.getQualifier());
                int d2 = Qualifiers.getQualifierDistance(ReflectionDesire.this.getQualifier(),
                                                         b2.getQualifier());
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
                (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction)) &&
                r.dfltSource.equals(dfltSource));
    }

    @Override
    public int hashCode() {
        return desiredType.hashCode() ^ injectPoint.hashCode() ^ (satisfaction == null ? 0 : satisfaction.hashCode()) ^ dfltSource.hashCode();
    }

    @Override
    public String toString() {
        return "ReflectionDesire(type=" + desiredType + ", inject=" + injectPoint + ", satisfaction=" + satisfaction + ", source=" + dfltSource + ")";
    }
}
