/*
 * Grapht, an open source dependency injector.
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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.reflect.ReflectionDesire.DefaultSource;
import org.grouplens.grapht.util.Types;

/**
 * ReflectionBindRule is an abstract implementation of BindRule. It is a partial
 * function from desires to desires. Its matching logic only depends on the
 * source type and Qualifier of the rule, and not what the function produces. A
 * ReflectionBindRule will only match a desire if the desire's desired type
 * equals the source type, and only if the desire's Qualifier inherits from the
 * Qualifier of the bind rule.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionBindRule implements BindRule {
    private final ReflectionSatisfaction satisfaction;
    private final boolean terminateChain;
    
    private final QualifierMatcher qualifier;
    private final Class<?> sourceType;
    private final Class<?> implType;
    
    private final int weight;

    /**
     * Create a bind rule that matches a desire when the desired type equals
     * <tt>sourceType</tt> and the desire's qualifier inherits from
     * <tt>qualifier</tt>. <tt>weight</tt> is an integer value that specifies
     * the priority between matching bind rules. Lower weights have a higher
     * priority.
     * 
     * @param sourceType The source type this bind rule matches
     * @param satisfaction The Satisfaction used by applied desires
     * @param qualifier The Qualifier the bind rule applies to
     * @param weight The weight or precedence of the rule
     * @param terminateChain True if the bind rule is a terminating rule
     * @throws NullPointerException if sourceType or satisfaction is null
     */
    public ReflectionBindRule(Class<?> sourceType, ReflectionSatisfaction satisfaction,
                              QualifierMatcher qualifier, int weight, boolean terminateChain) {
        Checks.notNull("source type", sourceType);
        Checks.notNull("satisfaction", satisfaction);
        Checks.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = satisfaction;
        this.implType = satisfaction.getErasedType();
        this.sourceType = Types.box(sourceType);
        this.weight = weight;
        this.terminateChain = terminateChain;
        
        // verify that the satisfaction produces proper types
        Checks.isAssignable(this.sourceType, this.implType);
    }
    
    /**
     * As the other constructor, but this is used for type to type bindings
     * where the implementation type is not yet instantiable, so there is no
     * satisfaction for the applied desires.
     * 
     * @param sourceType The source type this bind rule matches
     * @param implType The implementation type that is bound
     * @param qualifier The Qualifier the bind rule applies to
     * @param weight The weight or precedence of the rule
     * @param terminateChain True if the bind rule is a terminating rule
     * @throws NullPointerException if sourceType or implType is null
     */
    public ReflectionBindRule(Class<?> sourceType, Class<?> implType,
                              QualifierMatcher qualifier, int weight, boolean terminateChain) {
        Checks.notNull("source type", sourceType);
        Checks.notNull("implementation type", implType);
        Checks.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = null;
        this.implType = Types.box(implType);
        this.sourceType = Types.box(sourceType);
        this.weight = weight;
        this.terminateChain = terminateChain;
        
        // verify that implType extends sourceType
        Checks.isAssignable(this.sourceType, this.implType);
    }

    /**
     * @return The weight or precedence of the rule
     */
    @Override
    public int getWeight() {
        return weight;
    }
    
    /**
     * @return The annotation {@link QualifierMatcher} matched by this bind rule
     */
    public QualifierMatcher getQualifier() {
        return qualifier;
    }
    
    /**
     * @return The source type matched by this bind rule
     */
    public Class<?> getSourceType() {
        return sourceType;
    }
    
    public ReflectionSatisfaction getSatisfaction() {
        return satisfaction;
    }
    
    @Override
    public Desire apply(Desire desire) {
        ReflectionDesire input = (ReflectionDesire) desire;
        return new ReflectionDesire(implType, input.getInjectionPoint(), 
                                    satisfaction, DefaultSource.TYPE);
    }

    @Override
    public boolean terminatesChain() {
        return terminateChain;
    }
    
    @Override
    public boolean matches(Desire desire) {
        // bind rules match type by equality
        if (desire.getType().equals(sourceType)) {
            // if the type is equal, then rely on the qualifier matcher
            return qualifier.matches(desire.getAttributes().getQualifier());
        }
        
        // the type and {@link Qualifier}s are not a match, so return false
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionBindRule)) {
            return false;
        }
        ReflectionBindRule r = (ReflectionBindRule) o;
        return r.weight == weight && 
               r.sourceType.equals(sourceType) &&
               r.implType.equals(implType) &&
               r.terminateChain == terminateChain &&
               r.qualifier.equals(qualifier) &&
               (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction));
    }
    
    @Override
    public int hashCode() {
        int result = 17;

        result += 31 * result + weight;
        result += 31 * result + (terminateChain ? 1 : 0);
        result += 31 * result + sourceType.hashCode();
        result += 31 * result + implType.hashCode();
        result += 31 * result + qualifier.hashCode();

        if (satisfaction != null) {
            result += 31 * result + satisfaction.hashCode(); 
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        String i = (satisfaction == null ? implType.getSimpleName() : satisfaction.toString());
        return "Bind(weight=" + weight + ", " + qualifier + ":" + sourceType.getSimpleName() + " -> " + i + ")";
    }
}
