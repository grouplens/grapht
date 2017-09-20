/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
