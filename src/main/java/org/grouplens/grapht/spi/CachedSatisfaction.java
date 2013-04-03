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
package org.grouplens.grapht.spi;

import org.grouplens.grapht.util.Preconditions;

import java.io.Serializable;

/**
 * CachedSatisfaction is the pairing of a {@link Satisfaction} and
 * {@link CachePolicy}. Satisfaction cannot specify its own cache policy because
 * the policy is dependent on the edges and bindings taken to reach a particular
 * node in the graph.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class CachedSatisfaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final Satisfaction satisfaction;
    private final CachePolicy cachePolicy;
    
    /**
     * Create a new CachedSatisfaction wrapping the given satisfaction and the
     * satisfaction's default cache policy.
     * 
     * @param satisfaction The satisfaction to wrap
     * @throws NullPointerException if satisfaction is null
     */
    public CachedSatisfaction(Satisfaction satisfaction) {
        this(satisfaction, satisfaction.getDefaultCachePolicy());
    }
    
    /**
     * Create a new CachedSatisfaction wrapping the given satisfaction and cache
     * policy. Providers from the given satisfaction, used in conjunction with
     * this pair, must be wrapped to satisfy the chosen policy.
     * 
     * @param satisfaction The satisfaction to wrap
     * @param policy The policy used with this satisfaction
     * @throws NullPointerException if either argument is null
     */
    public CachedSatisfaction(Satisfaction satisfaction, CachePolicy policy) {
        Preconditions.notNull("satisfaction", satisfaction);
        Preconditions.notNull("policy", policy);
        
        this.satisfaction = satisfaction;
        cachePolicy = policy;
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
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CachedSatisfaction)) {
            return false;
        }
            
        CachedSatisfaction c = (CachedSatisfaction) o;
        return c.satisfaction.equals(satisfaction) && c.cachePolicy.equals(cachePolicy);
    }
    
    @Override
    public int hashCode() {
        return satisfaction.hashCode() ^ cachePolicy.hashCode();
    }
    
    @Override
    public String toString() {
        return "(" + satisfaction + ", " + cachePolicy + ")";
    }
}
