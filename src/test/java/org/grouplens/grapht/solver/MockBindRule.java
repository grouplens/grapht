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

import java.util.HashMap;
import java.util.Map;

import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.Desire;

/**
 * MockBindRule is a simple implementation of BindRule where the matching
 * bindings are represented as a map from input Desires to output Desires, via
 * {@link #addMapping(Desire, Desire)}.
 * 
 * @author Michael Ludwig
 */
public class MockBindRule implements BindRule {
    private static final long serialVersionUID = 1L;
    
    private final Map<Desire, Desire> bindings;
    private boolean terminate;
    
    public MockBindRule() {
        bindings = new HashMap<Desire, Desire>();
    }
    
    public MockBindRule(Desire in, Desire out) {
        this();
        addMapping(in, out);
    }
    
    public void setTerminatesChain(boolean terminate) {
        this.terminate = terminate;
    }
    
    public void addMapping(Desire in, Desire out) {
        bindings.put(in, out);
    }
    
    @Override
    public boolean matches(Desire desire) {
        return bindings.containsKey(desire);
    }

    @Override
    public Desire apply(Desire desire) {
        return bindings.get(desire);
    }

    @Override
    public int getWeight() {
        return 0;
    }

    @Override
    public boolean terminatesChain() {
        return terminate;
    }
}
