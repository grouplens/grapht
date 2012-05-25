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

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;

/**
 * Thrown when a desire has too many BindRules that match and there is no
 * single best rule to apply from the set of matched rules.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MultipleBindingsException extends SolverException {
    private static final long serialVersionUID = 1L;

    private final List<Pair<Satisfaction, Attributes>> context;
    private final List<Desire> desireChain;
    private final List<BindRule> bindRules;
    
    public MultipleBindingsException(List<Pair<Satisfaction, Attributes>> context, List<Desire> desireChain, List<BindRule> bindRules) {
        this.context = Collections.unmodifiableList(new ArrayList<Pair<Satisfaction, Attributes>>(context));
        this.desireChain = Collections.unmodifiableList(new ArrayList<Desire>(desireChain));
        this.bindRules = Collections.unmodifiableList(new ArrayList<BindRule>(bindRules));
    }
    
    /**
     * @return The context that produced the problematic bindings
     */
    public List<Pair<Satisfaction, Attributes>> getContext() {
        return context;
    }
    
    /**
     * @return The top bind rules that could not be reduced to a single rule,
     *         the list will have at least 2 elements
     */
    public List<BindRule> getBindRules() {
        return bindRules;
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
        StringBuilder sb = new StringBuilder("Too many choices for desire: ")
            .append(formatDesire(desireChain.get(desireChain.size() - 1)))
            .append('\n');
        
        // bind rules
        sb.append("Possible bindings:\n");
        for (BindRule rule: bindRules) {
            sb.append('\t').append(formatBindRule(rule)).append('\n');
        }
        sb.append('\n');
        
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
