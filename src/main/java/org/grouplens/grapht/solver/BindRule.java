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
package org.grouplens.grapht.solver;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * BindRule is a partial function from desire to desire that acts as a binding.
 * The {@link RuleBasedBindingFunction} takes a collection of BindRules grouped
 * into their activating contexts to form a {@link BindingFunction}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class BindRule implements Externalizable {
    // "final"
    private Satisfaction satisfaction;
    private boolean terminateChain;
    
    private QualifierMatcher qualifier;
    private Class<?> sourceType;
    private Class<?> implType;
    
    private CachePolicy policy;
        
    /**
     * Create a bind rule that matches a desire when the desired type equals
     * <tt>sourceType</tt> and the desire's qualifier matches <tt>qualifier</tt>
     * .
     * 
     * @param sourceType The source type this bind rule matches
     * @param satisfaction The Satisfaction used by applied desires
     * @param policy The CachePolicy for nodes created by this bind rule
     * @param qualifier The Qualifier the bind rule applies to
     * @param terminateChain True if the bind rule is a terminating rule
     * @throws NullPointerException if arguments are null
     */
    public BindRule(Class<?> sourceType, Satisfaction satisfaction, CachePolicy policy,
                    QualifierMatcher qualifier, boolean terminateChain) {
        Preconditions.notNull("source type", sourceType);
        Preconditions.notNull("satisfaction", satisfaction);
        Preconditions.notNull("policy", policy);
        Preconditions.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = satisfaction;
        this.implType = satisfaction.getErasedType();
        this.policy = policy;
        this.sourceType = Types.box(sourceType);
        this.terminateChain = terminateChain;
        
        // verify that the satisfaction produces proper types
        Preconditions.isAssignable(this.sourceType, this.implType);
    }
    
    /**
     * As the other constructor, but this is used for type to type bindings
     * where the implementation type is not yet instantiable, so there is no
     * satisfaction for the applied desires.
     * 
     * @param sourceType The source type this bind rule matches
     * @param implType The implementation type that is bound
     * @param policy The CachePolicy for nodes created by this bind rule
     * @param qualifier The Qualifier the bind rule applies to
     * @param terminateChain True if the bind rule is a terminating rule
     * @throws NullPointerException if arguments are null
     */
    public BindRule(Class<?> sourceType, Class<?> implType, CachePolicy policy,
                    QualifierMatcher qualifier, boolean terminateChain) {
        Preconditions.notNull("source type", sourceType);
        Preconditions.notNull("implementation type", implType);
        Preconditions.notNull("policy", policy);
        Preconditions.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = null;
        this.implType = Types.box(implType);
        this.policy = policy;
        this.sourceType = Types.box(sourceType);
        this.terminateChain = terminateChain;
        
        // verify that implType extends sourceType
        Preconditions.isAssignable(this.sourceType, this.implType);
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public BindRule() { }
    
    /**
     * @return The annotation {@link QualifierMatcher} matched by this bind rule
     */
    public QualifierMatcher getQualifier() {
        return qualifier;
    }
    
    /**
     * @return The CachePolicy to use for satisfactions created with this rule
     */
    public CachePolicy getCachePolicy() {
        return policy;
    }
    
    /**
     * Apply this BindRule to the given Desire, and return a restricted and
     * possibly satisfied desire. It is assumed that {@link #matches(Desire)}
     * returns true.
     * 
     * @param desire The desire that is input to this partial binding function
     * @return The restricted desire
     */
    public Desire apply(Desire desire) {
        if (satisfaction != null) {
            return desire.restrict(satisfaction);
        } else {
            return desire.restrict(implType);
        }
    }

    /**
     * @return True if this should be the last bind rule applied to the desire
     *         chain when attempting to find a satisfaction
     */
    public boolean terminatesChain() {
        return terminateChain;
    }
    
    /**
     * @param desire The input desire
     * @return True if this desire matches this BindRule and can be passed to
     *         {@link #apply(Desire)}
     */
    public boolean matches(Desire desire) {
        // bind rules match type by equality
        if (desire.getDesiredType().equals(sourceType)) {
            // if the type is equal, then rely on the qualifier matcher
            return qualifier.matches(desire.getInjectionPoint().getAttributes().getQualifier());
        }
        
        // the type and {@link Qualifier}s are not a match, so return false
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BindRule)) {
            return false;
        }
        BindRule r = (BindRule) o;
        return r.sourceType.equals(sourceType) &&
               r.implType.equals(implType) &&
               r.terminateChain == terminateChain &&
               r.qualifier.equals(qualifier) &&
               r.policy.equals(policy) &&
               (r.satisfaction == null ? satisfaction == null : r.satisfaction.equals(satisfaction));
    }
    
    @Override
    public int hashCode() {
        int result = 17;

        result += 31 * result + (terminateChain ? 1 : 0);
        result += 31 * result + sourceType.hashCode();
        result += 31 * result + implType.hashCode();
        result += 31 * result + qualifier.hashCode();
        result += 31 * result + policy.hashCode();

        if (satisfaction != null) {
            result += 31 * result + satisfaction.hashCode(); 
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        String i = (satisfaction == null ? implType.getSimpleName() : satisfaction.toString());
        return "Bind(" + qualifier + ":" + sourceType.getSimpleName() + " -> " + i + ")";
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        sourceType = Types.readClass(in);
        implType = Types.readClass(in);
        
        satisfaction = (Satisfaction) in.readObject();
        qualifier = (QualifierMatcher) in.readObject();
        policy = (CachePolicy) in.readObject();
        
        terminateChain = in.readBoolean();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeClass(out, sourceType);
        Types.writeClass(out, implType);
        
        out.writeObject(satisfaction);
        out.writeObject(qualifier);
        out.writeObject(policy);

        out.writeBoolean(terminateChain);
    }
}
