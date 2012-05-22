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
package org.grouplens.grapht.spi;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.grouplens.grapht.util.Pair;

/**
 * ContextChain represents a list of ContextMatchers. ContextMatchers can match
 * a single node within a context, and a ContextChain can match an entire
 * context. A ContextChain matches a context if its context matchers match a
 * subsequence of the nodes within the context.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ContextChain implements Externalizable {
    // "final"
    private List<ContextMatcher> matchers;

    /**
     * Create a new ContextChain representing the empty context without any
     * matchers.
     */
    public ContextChain() {
        this(new ArrayList<ContextMatcher>());
    }

    /**
     * Create a new ContextChain with the given context matchers. The var-args
     * parameter forms a list in the order the arguments are passed in.
     * Arguments should not be null.
     * 
     * @param contextMatchers The var-args of matchers to use in this chain
     */
    public ContextChain(ContextMatcher... contextMatchers) {
        this(Arrays.asList(contextMatchers));
    }
    
    /**
     * Create a new ContextChain that matches the given sequence of
     * ContextMatchers. The list is copied so the created ContextChain is
     * immutable. The list should not contain any null elements.
     * 
     * @param matchers The matcher list this chain represents
     * @throws NullPointerException if matchers is null
     */
    public ContextChain(List<? extends ContextMatcher> matchers) {
        this.matchers = new ArrayList<ContextMatcher>(matchers);
    }
    
    /**
     * @return An unmodifiable list of the context matchers in this chain
     */
    public List<ContextMatcher> getContexts() {
        return Collections.unmodifiableList(matchers);
    }

    /**
     * <p>
     * Return whether or not this chain of ContextMatchers matches the actual
     * context represented by the ordered list of Satisfaction and Roles. The
     * start of the context represents the root of the dependency path.
     * <p>
     * This returns true if the sequence of context matchers is a subsequence of
     * the contexts, with respect to the matcher's
     * {@link ContextMatcher#matches(Pair) match()} method.
     * <p>
     * Given this definition, a ContextChain with no matchers will match every
     * real context.
     * 
     * @param nodes The current context
     * @return True if this chain matches
     */
    public boolean matches(List<Pair<Satisfaction, Attributes>> nodes) {
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
    
    @Override
    public String toString() {
        return "ContextChain(" + matchers.toString() + ")";
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(matchers.size());
        for (ContextMatcher m: matchers) {
            out.writeObject(m);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int count = in.readInt();
        matchers = new ArrayList<ContextMatcher>(count);
        for (int i = 0; i < count; i++) {
            matchers.add((ContextMatcher) in.readObject());
        }
    }
}
