package org.grouplens.grapht.spi;

import java.io.Serializable;

import org.grouplens.grapht.util.Preconditions;

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
    
    // "final"
    private Satisfaction satisfaction;
    private CachePolicy cachePolicy;
    
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
