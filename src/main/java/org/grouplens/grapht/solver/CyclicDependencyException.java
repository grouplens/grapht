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

/**
 * Thrown by when a cyclic dependency is detected and could not be broken or
 * bypassed by the solver.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class CyclicDependencyException extends ResolverException {
    private static final long serialVersionUID = 1L;

    private final Desire desire;
    
    public CyclicDependencyException(Desire desire, String msg) {
        super(msg);
        this.desire = desire;
    }
    
    /**
     * @return The current desire that triggered the cycle detection
     */
    public Desire getDesire() {
        return desire;
    }
    
    @Override
    public String getMessage() {
        // header
        StringBuilder sb = new StringBuilder(super.getMessage())
            .append('\n');
        
        // current desire
        sb.append("Unsatisfiable desire:\n")
          .append('\t')
          .append(formatDesire(desire))
          .append('\n');
        
        return sb.toString();
    }
}