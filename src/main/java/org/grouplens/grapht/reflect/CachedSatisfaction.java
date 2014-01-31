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
package org.grouplens.grapht.reflect;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.util.Preconditions;

import java.io.Serializable;
import java.util.UUID;

/**
 * CachedSatisfaction is the pairing of a {@link Satisfaction} and
 * {@link CachePolicy}. Satisfaction cannot specify its own cache policy because
 * the policy is dependent on the edges and bindings taken to reach a particular
 * node in the graph.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class CachedSatisfaction implements Serializable {
    private static final long serialVersionUID = 4L;
    
    private final Satisfaction satisfaction;
    private final CachePolicy cachePolicy;
    private final boolean fixed;
    private final UUID unique;

    /**
     * Create a new CachedSatisfaction wrapping the given satisfaction and the
     * satisfaction's default cache policy.
     * 
     * @param satisfaction The satisfaction to wrap
     * @throws NullPointerException if satisfaction is null
     */
    public CachedSatisfaction(Satisfaction satisfaction) {
        this(satisfaction, satisfaction.getDefaultCachePolicy(), false);
    }

    /**
     * Create a new CachedSatisfaction wrapping the given satisfaction and cache policy. Providers
     * from the given satisfaction, used in conjunction with this pair, must be wrapped to satisfy
     * the chosen policy.
     *
     * @param satisfaction The satisfaction to wrap
     * @param policy       The policy used with this satisfaction
     * @throws NullPointerException the satisfaction or policy is null
     */
    public CachedSatisfaction(Satisfaction satisfaction, CachePolicy policy) {
        this(satisfaction, policy, false);
    }

    /**
     * Create a new CachedSatisfaction wrapping the given satisfaction and cache policy. Providers
     * from the given satisfaction, used in conjunction with this pair, must be wrapped to satisfy
     * the chosen policy.
     *
     * @param satisfaction The satisfaction to wrap
     * @param policy       The policy used with this satisfaction
     * @param fixed        {@code true} if this satisfaction is <emph>fixed</emph>, meaning that it
     *                     will not be removed by {@link org.grouplens.grapht.solver.DependencySolver#rewrite(org.grouplens.grapht.graph.DAGNode)}.
     * @throws NullPointerException if either argument is null
     */
    public CachedSatisfaction(Satisfaction satisfaction, CachePolicy policy, boolean fixed) {
        this(satisfaction, policy, fixed, null);
    }

    private CachedSatisfaction(Satisfaction satisfaction, CachePolicy policy, boolean fx, UUID key) {
        Preconditions.notNull("satisfaction", satisfaction);
        Preconditions.notNull("policy", policy);

        this.satisfaction = satisfaction;
        cachePolicy = policy;
        fixed = fx;
        unique = key;
    }

    /**
     * @return The Satisfaction stored in this pair
     */
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }
    
    /**
     * @return The CachePolicy that must be implemented on top of the providers
     *         from this {@link #getSatisfaction() satisfaction}
     */
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    /**
     * Make a copy of this satisfaction which will not compare equal to any other satisfaction.
     * @return A unique copy of the satisfaction.
     */
    public CachedSatisfaction uniqueCopy() {
        return new CachedSatisfaction(satisfaction, cachePolicy, fixed, UUID.randomUUID());
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CachedSatisfaction)) {
            return false;
        }
            
        CachedSatisfaction c = (CachedSatisfaction) o;
        EqualsBuilder eqb = new EqualsBuilder();
        return eqb.append(satisfaction, c.satisfaction)
                  .append(cachePolicy, c.cachePolicy)
                  .append(unique, c.unique)
                  .isEquals();
    }
    
    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.append(satisfaction)
                  .append(cachePolicy)
                  .append(unique)
                  .toHashCode();
    }
    
    @Override
    public String toString() {
        return "(" + satisfaction + ", " + cachePolicy + ")";
    }
}
