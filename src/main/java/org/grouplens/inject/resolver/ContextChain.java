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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContextChain {
    private final List<ContextMatcher> matchers;
    
    public ContextChain(List<? extends ContextMatcher> matchers) {
        this.matchers = new ArrayList<ContextMatcher>(matchers);
    }
    
    public List<ContextMatcher> getContexts() {
        return Collections.unmodifiableList(matchers);
    }
    
    public boolean matches(List<SatisfactionAndRole> nodes) {
        int i = 0;
        for (ContextMatcher cm: matchers) {
            boolean found = false;
            // search forward in the list of nodes for the
            // first node to match the current matcher
            for (int j = i; j < nodes.size(); j++) {
                if (cm.matches(nodes.get(j))) {
                    // node found, so record the next starting index
                    found = true;
                    i = j + 1;
                    break;
                }
            }
            
            if (!found) {
                // no matching context element was found
                // FIXME: this is a greedy algorithm, can it be proven to always
                // be valid given that this is not equality but inheritence checking?
                return false;
            }
        }
        
        // we've found matching nodes for every context matcher,
        // in the same order as context matchers
        return true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContextChain))
            return false;
        return ((ContextChain) o).matchers.equals(matchers);
    }
    
    @Override
    public int hashCode() {
        return matchers.hashCode();
    }
}
