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

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.util.Preconditions;

/**
 * BindingResult is the result tuple of a {@link BindingFunction}. It is
 * effectively a {@link Desire} with additional metadata needed to implement
 * certain features within the dependency solver.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class BindingResult {
    private final Desire desire;
    private final CachePolicy policy;
    private final boolean defer;
    private final boolean terminate;
    
    /**
     * Create a new result that wraps the given Desire.
     * 
     * @param desire The resultant desire from a BindingFunction
     * @param policy The CachePolicy for this binding
     * @param defer True if the desire should be processed later (including any
     *            dependencies)
     * @param terminate True if no more functions should be applied to the
     *            desire
     * @throws NullPointerException if desire or policy is null
     */
    public BindingResult(Desire desire, CachePolicy policy, 
                         boolean defer, boolean terminate) {
        Preconditions.notNull("desire", desire);
        Preconditions.notNull("policy", policy);
        
        this.policy = policy;
        this.desire = desire;
        this.defer = defer;
        this.terminate = terminate;
    }
    
    /**
     * @return The restricted desire result of the binding function
     */
    public Desire getDesire() {
        return desire;
    }
    
    /**
     * @return The CachePolicy for this binding
     */
    public CachePolicy getCachePolicy() {
        return policy;
    }
    
    /**
     * @return True if the resulting desire should be deferred until all other
     *         desires in this phase have been completed
     */
    public boolean isDeferred() {
        return defer;
    }
    
    /**
     * @return True if no more binding functions should process the resulting
     *         desire
     */
    public boolean terminates() {
        return terminate;
    }
}
