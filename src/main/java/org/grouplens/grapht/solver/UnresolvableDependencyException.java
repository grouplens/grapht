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
package org.grouplens.grapht.solver;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;

import java.util.List;

/**
 * Thrown when a desire cannot be resolved to an instantiable satisfaction.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class UnresolvableDependencyException extends ResolutionException {
    private static final long serialVersionUID = 1L;

    private final DesireChain desires;
    private final InjectionContext context;
    
    public UnresolvableDependencyException(DesireChain chain, InjectionContext context) {
        this.desires = chain;
        this.context = context;
    }
    
    /**
     * Get the context for this error.
     *
     * @return The context that produced the unresolvable desire
     */
    public InjectionContext getContext() {
        return context;
    }

    /**
     * Get the entire desire chain in which resolution failed.
     * @return The desire chain that failed to resolve.
     */
    public DesireChain getDesireChain() {
        return desires;
    }
    
    /**
     * Get the desire that failed to resolve.
     *
     * @return The unresolvable desire
     */
    public Desire getDesire() {
        return desires.getCurrentDesire();
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Unable to satisfy desire ")
                .append(format(desires.getCurrentDesire().getInjectionPoint()));
        List<Pair<Satisfaction, InjectionPoint>> path = context;
        if (!path.isEmpty()) {
            sb.append(" of ")
              .append(path.get(0).getLeft());
        }
        sb.append('\n')
          .append(format(context, desires));
        return sb.toString();
    }
}
