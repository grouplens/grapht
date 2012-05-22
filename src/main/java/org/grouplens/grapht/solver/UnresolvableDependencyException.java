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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Pair;

/**
 * Thrown when a desire cannot be resolved to an instantiable satisfaction.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class UnresolvableDependencyException extends ResolverException {
    private static final long serialVersionUID = 1L;

    private final List<Pair<Satisfaction, Attributes>> context;
    private final List<Desire> desireChain;
    
    public UnresolvableDependencyException(List<Pair<Satisfaction, Attributes>> context, List<Desire> desireChain) {
        this.context = Collections.unmodifiableList(new ArrayList<Pair<Satisfaction, Attributes>>(context));
        this.desireChain = Collections.unmodifiableList(new ArrayList<Desire>(desireChain));
    }
    
    /**
     * @return The context that produced the unresolvable desire
     */
    public List<Pair<Satisfaction, Attributes>> getContext() {
        return context;
    }
    
    /**
     * <p>
     * Get the list of desires that were being resolved. The first Desire in the
     * list represents the desire exposed by the last satisfaction in the
     * context. The last desire is the desire that matched too many bind rules.
     * <p>
     * Any desires between those two are intermediate desires that were the
     * result of applying other rules.
     * 
     * @return The desire chain that produced too many rules
     */
    public List<Desire> getDesires() {
        return desireChain;
    }
    
    @Override
    public String getMessage() {
        // header
        StringBuilder sb = new StringBuilder("Unable to satisfy desire: ")
            .append(formatDesire(desireChain.get(desireChain.size() - 1)))
            .append('\n');
        
        // context
        sb.append("Current context:\n");
        for (Pair<Satisfaction, Attributes> ctx: context) {
            sb.append('\t').append(formatContext(ctx)).append('\n');
        }
        sb.append('\n');
        
        // desire chain
        sb.append("Desire resolution:\n");
        for (Desire desire: desireChain) {
            sb.append('\t').append(formatDesire(desire)).append('\n');
        }
        
        return sb.toString();
    }
}
