/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.resolver;

import javax.annotation.Nullable;

import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Role;
import org.grouplens.inject.graph.Satisfaction;

/**
 * SatisfactionAndRole is a simple pairing between a Satisfaction and a Role. Roles are defined
 * as part of a dependency {@link Desire}. However, when resolving all immediate
 * and transient dependencies for a Satisfaction, it can be useful to keep track of the
 * Role of the desire that the Satisfaction is satisfying.
 * 
 * @see ContextMatcher
 * @see ContextChain
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class SatisfactionAndRole {
    private final Satisfaction satisfaction;
    private final Role role;

    /**
     * Create a new SatisfactionAndRole pair between the given Satisfaction and Role. The role
     * can be null to represent the default role.
     * 
     * @param satisfaction The Satisfaction satisfying some dependency or desire
     * @param role The role on the desire that the satisfaction satisfies
     * @throws NullPointerException if satisfaction is null
     */
    public SatisfactionAndRole(Satisfaction satisfaction, @Nullable Role role) {
        if (satisfaction == null)
            throw new NullPointerException("Satisfaction cannot be null");
        
        this.satisfaction = satisfaction;
        this.role = role;
    }
    
    /**
     * @return The Satisfaction satisfying some desire, will not be null
     */
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }
    
    /**
     * @return The role on the desire that the Node satisfies, may be null
     */
    public Role getRole() {
        return role;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SatisfactionAndRole))
            return false;
        SatisfactionAndRole n = (SatisfactionAndRole) o;
        return n.satisfaction.equals(satisfaction) && (n.role == null ? role == null : n.role.equals(role));
    }
}