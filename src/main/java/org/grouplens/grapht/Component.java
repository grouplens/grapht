/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.util.Preconditions;

import java.io.Serializable;

/**
 * A component to be instantiated in the final dependency plan.  A component consists of a {@link
 * Satisfaction} and related information for instantiating it (such as the {@link CachePolicy}).
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class Component implements Serializable {
    private static final long serialVersionUID = 5L;
    
    private final Satisfaction satisfaction;
    private final CachePolicy cachePolicy;

    private Component(Satisfaction satisfaction, CachePolicy policy) {
        Preconditions.notNull("satisfaction", satisfaction);
        Preconditions.notNull("policy", policy);

        this.satisfaction = satisfaction;
        cachePolicy = policy;
    }

    /**
     * Create a new Component wrapping the given satisfaction and cache policy.  The injector is
     * responsible for using the satisfaction to implement this component consistent with its
     * cache policy.
     *
     * @param satisfaction The satisfaction to wrap
     * @param policy       The policy used with this satisfaction
     * @throws NullPointerException the satisfaction or policy is null
     */
    public static Component create(Satisfaction satisfaction, CachePolicy policy) {
        return new Component(satisfaction, policy);
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
        if (!(o instanceof Component)) {
            return false;
        }
            
        Component c = (Component) o;
        EqualsBuilder eqb = new EqualsBuilder();
        return eqb.append(satisfaction, c.satisfaction)
                  .append(cachePolicy, c.cachePolicy)
                  .isEquals();
    }
    
    @Override
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        return hcb.append(satisfaction)
                  .append(cachePolicy)
                  .toHashCode();
    }
    
    @Override
    public String toString() {
        return "(" + satisfaction + ", " + cachePolicy + ")";
    }
}
