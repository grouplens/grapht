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

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nullable;
import java.io.*;

/**
 * Foundational implementation of {@link BindRule}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class BindRuleImpl implements BindRule, Serializable {
    private static final long serialVersionUID = -1L;

    private final Satisfaction satisfaction;
    private final boolean terminateChain;
    
    private final QualifierMatcher qualifier;
    private final Class<?> depType;
    private final Class<?> implType;
    
    private final CachePolicy policy;
        
    /**
     * Create a bind rule that matches a desire when the desired type equals
     * <tt>depType</tt> and the desire's qualifier matches <tt>qualifier</tt>
     * .
     * 
     * @param depType The dependency type this bind rule matches
     * @param satisfaction The Satisfaction used by applied desires
     * @param policy The CachePolicy for nodes created by this bind rule
     * @param qualifier The Qualifier the bind rule applies to
     * @param terminateChain True if the bind rule is a terminating rule
     * @throws NullPointerException if arguments are null
     */
    public BindRuleImpl(Class<?> depType, Satisfaction satisfaction, CachePolicy policy,
                        QualifierMatcher qualifier, boolean terminateChain) {
        Preconditions.notNull("dependency type", depType);
        Preconditions.notNull("satisfaction", satisfaction);
        Preconditions.notNull("policy", policy);
        Preconditions.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = satisfaction;
        this.implType = satisfaction.getErasedType();
        this.policy = policy;
        this.depType = Types.box(depType);
        this.terminateChain = terminateChain;
        
        // verify that the satisfaction produces proper types
        Preconditions.isAssignable(this.depType, this.implType);
    }
    
    /**
     * As the other constructor, but this is used for type to type bindings
     * where the implementation type is not yet instantiable, so there is no
     * satisfaction for the applied desires.
     * 
     * @param depType The dependency type this bind rule matches
     * @param implType The implementation type that is bound
     * @param policy The CachePolicy for nodes created by this bind rule
     * @param qualifier The Qualifier the bind rule applies to
     * @param terminateChain True if the bind rule is a terminating rule
     * @throws NullPointerException if arguments are null
     */
    public BindRuleImpl(Class<?> depType, Class<?> implType, CachePolicy policy,
                        QualifierMatcher qualifier, boolean terminateChain) {
        Preconditions.notNull("dependency type", depType);
        Preconditions.notNull("implementation type", implType);
        Preconditions.notNull("policy", policy);
        Preconditions.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = null;
        this.implType = Types.box(implType);
        this.policy = policy;
        this.depType = Types.box(depType);
        this.terminateChain = terminateChain;
        
        // verify that implType extends depType
        Preconditions.isAssignable(this.depType, this.implType);
    }

    @Override
    public QualifierMatcher getQualifier() {
        return qualifier;
    }
    
    @Override
    public CachePolicy getCachePolicy() {
        return policy;
    }
    
    @Override
    public Desire apply(Desire desire) {
        if (satisfaction != null) {
            return desire.restrict(satisfaction);
        } else {
            return desire.restrict(implType);
        }
    }

    @Override
    public boolean terminatesChain() {
        return terminateChain;
    }
    
    @Override
    public boolean matches(Desire desire) {
        // bind rules match type by equality
        if (desire.getDesiredType().equals(depType)) {
            // if the type is equal, then rely on the qualifier matcher
            return qualifier.matches(desire.getInjectionPoint().getAttributes().getQualifier());
        }
        
        // the type and {@link Qualifier}s are not a match, so return false
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BindRuleImpl)) {
            return false;
        }
        BindRuleImpl r = (BindRuleImpl) o;
        return r.depType.equals(depType) &&
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
        result += 31 * result + depType.hashCode();
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
        return "Bind(" + qualifier + ":" + depType.getSimpleName() + " -> " + i + ")";
    }

    private Object writeReplace() {
        return new SerialProxy(satisfaction, terminateChain, qualifier,
                               depType, implType, policy);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    /**
     * Serialization proxy class.
     */
    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ClassProxy depType;
        private final QualifierMatcher qualifier;
        private final ClassProxy implType;

        @Nullable
        private final Satisfaction satisfaction;
        private final boolean terminal;
        private final CachePolicy cachePolicy;

        private SerialProxy(@Nullable Satisfaction sat, boolean term, QualifierMatcher qmatch,
                            Class<?> stype, Class<?> itype, CachePolicy policy) {
            satisfaction = sat;
            terminal = term;
            qualifier = qmatch;
            depType = ClassProxy.of(stype);
            implType = ClassProxy.of(itype);
            cachePolicy = policy;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                if (satisfaction == null) {
                    return new BindRuleImpl(depType.resolve(), implType.resolve(),
                                        cachePolicy, qualifier, terminal);
                } else {
                    return new BindRuleImpl(depType.resolve(), satisfaction,
                                        cachePolicy, qualifier, terminal);
                }
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("cannot resolve type");
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
