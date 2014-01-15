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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.reflect.CachePolicy;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Foundational implementation of {@link BindRule}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
final class BindRuleImpl implements BindRule, Serializable {
    private static final long serialVersionUID = -1L;

    private final Satisfaction satisfaction;
    private final boolean terminal;
    
    private final QualifierMatcher qualifier;
    private final Class<?> depType;
    private final Class<?> implType;
    
    private final CachePolicy policy;

    private transient volatile int hashCode;
        
    /**
     * Create a bind rule that matches a desire when the desired type equals
     * <tt>depType</tt> and the desire's qualifier matches <tt>qualifier</tt>
     * .
     * 
     * @param depType The dependency type this bind rule matches
     * @param satisfaction The Satisfaction used by applied desires
     * @param policy The CachePolicy for nodes created by this bind rule
     * @param qualifier The Qualifier the bind rule applies to
     * @param terminal True if the bind rule is a terminating rule (see {@link #isTerminal()}).
     * @throws NullPointerException if arguments are null
     */
    public BindRuleImpl(@Nonnull Class<?> depType,
                        @Nonnull Satisfaction satisfaction,
                        @Nonnull CachePolicy policy,
                        @Nonnull QualifierMatcher qualifier,
                        boolean terminal) {
        Preconditions.notNull("dependency type", depType);
        Preconditions.notNull("satisfaction", satisfaction);
        Preconditions.notNull("policy", policy);
        Preconditions.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = satisfaction;
        this.implType = satisfaction.getErasedType();
        this.policy = policy;
        this.depType = Types.box(depType);
        this.terminal = terminal;
        
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
     * @param terminal True if the bind rule is a terminating rule (see {@link #isTerminal()})
     * @throws NullPointerException if arguments are null
     */
    public BindRuleImpl(@Nonnull Class<?> depType,
                        @Nonnull Class<?> implType,
                        @Nonnull CachePolicy policy,
                        @Nonnull QualifierMatcher qualifier,
                        boolean terminal) {
        Preconditions.notNull("dependency type", depType);
        Preconditions.notNull("implementation type", implType);
        Preconditions.notNull("policy", policy);
        Preconditions.notNull("qualifier matcher", qualifier);
        
        this.qualifier = qualifier;
        this.satisfaction = null;
        this.implType = Types.box(implType);
        this.policy = policy;
        this.depType = Types.box(depType);
        this.terminal = terminal;
        
        // verify that implType extends depType
        Preconditions.isAssignable(this.depType, this.implType);
    }

    /**
     * Get the rule's qualifier matcher.
     *
     * @return The annotation {@link QualifierMatcher} matched by this bind rule.
     */
    public QualifierMatcher getQualifierMatcher() {
        return qualifier;
    }

    @Override
    public CachePolicy getCachePolicy() {
        return policy;
    }
    
    @Override
    public Desire apply(Desire desire) {
        // TODO Separate bind rules into different classes based on sat vs. type targets
        if (satisfaction != null) {
            return desire.restrict(satisfaction);
        } else {
            return desire.restrict(implType);
        }
    }

    @Override
    public boolean isTerminal() {
        return terminal;
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
    public BindRuleBuilder newCopyBuilder() {
        BindRuleBuilder bld = new BindRuleBuilder();
        bld.setDependencyType(depType)
           .setQualifierMatcher(qualifier)
           .setCachePolicy(policy)
           .setTerminal(terminal);
        if (satisfaction != null) {
            bld.setSatisfaction(satisfaction);
        } else {
            bld.setImplementation(implType);
        }
        return bld;
    }

    @Override
    public int compareTo(BindRule other) {
        if (other instanceof BindRuleImpl) {
            return qualifier.compareTo(((BindRuleImpl) other).qualifier);
        } else {
            throw new IllegalArgumentException("incompatible bind rule");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof BindRuleImpl) {
            EqualsBuilder eq = new EqualsBuilder();
            BindRuleImpl or = (BindRuleImpl) o;
            return eq.append(depType, or.depType)
                    .append(implType, or.implType)
                    .append(terminal, or.terminal)
                    .append(qualifier, or.qualifier)
                    .append(policy, or.policy)
                    .append(satisfaction, or.satisfaction)
                    .isEquals();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        if (hashCode == 0) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hashCode = hcb.append(terminal)
                          .append(depType)
                          .append(implType)
                          .append(qualifier)
                          .append(policy)
                          .append(satisfaction)
                          .toHashCode();
        }
        return hashCode;
    }
    
    @Override
    public String toString() {
        String i = (satisfaction == null ? implType.getSimpleName() : satisfaction.toString());
        return "Bind(" + qualifier + ":" + depType.getSimpleName() + " -> " + i + ")";
    }

    private Object writeReplace() {
        return new SerialProxy(satisfaction, terminal, qualifier,
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
