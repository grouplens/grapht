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

import org.grouplens.grapht.reflect.Desire;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Thrown when a BindingFunction would be required to return multiple binding
 * results for a given desire and context. This is not thrown when multiple
 * functions are each capable of producing a single result for a desire, since
 * binding functions are given a priority.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MultipleBindingsException extends SolverException {
    private static final long serialVersionUID = 1L;

    private final InjectionContext context;
    private final DesireChain desires;
    private final Collection<?> bindings;
    
    public MultipleBindingsException(DesireChain desires, InjectionContext context, Collection<?> bindings) {
        this.desires = desires;
        this.context = context;
        this.bindings = Collections.unmodifiableCollection(new ArrayList<Object>(bindings));
    }
    
    /**
     * @return The context that produced the problematic bindings
     */
    public InjectionContext getContext() {
        return context;
    }
    
    /**
     * @return The possible bindings, which depends on the BindingFunction that
     * produced this exception
     */
    public Collection<?> getBindRules() {
        return bindings;
    }
    
    /**
     * @return The desire that had too many possible binding within a
     *         BindingFunction
     */
    public Desire getDesire() {
        return desires.getCurrentDesire();
    }
    
    @Override
    public String getMessage() {
        return new StringBuilder("Too many choices for desire: ")
            .append(format(getDesire().getInjectionPoint()))
            .append('\n')
            .append(format(context, desires))
            .toString();
    }
}
