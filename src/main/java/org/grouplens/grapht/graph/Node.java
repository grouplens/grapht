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
package org.grouplens.grapht.graph;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Satisfaction;

import javax.annotation.Nullable;
import java.io.Serializable;


/**
 * <p>
 * A Node represents a conceptual node within a graph. Nodes within the same
 * graph can contain label instances that represent domain-specific data for
 * the graph.
 * <p>
 * Nodes use instance equality, regardless of how their labels might implement
 * equality or how they compare. Thus, if two Node instances in a graph have the
 * same label, they are still considered separate nodes from the graph's
 * perspective.
 * 
 * @see Edge
 * @see Graph
 * @author Michael Ludwig <mludwig@cs.umn.edu
 */
public class Node implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private final CachedSatisfaction label;
    
    /**
     * Create a new Node with a null label.
     */
    public Node() {
        this((CachedSatisfaction) null);
    }
    
    /**
     * Create a new Node that uses the given satisfaction and its default cache
     * policy. If the satisfaction is null, the created CachedSatisfaction label
     * will be null.
     * 
     * @param satisfaction The satisfaction attached to this node
     */
    public Node(@Nullable Satisfaction satisfaction) {
        this((satisfaction == null ? null : new CachedSatisfaction(satisfaction)));
    }
    
    /**
     * Create a new Node that uses the given satisfaction and cache policy. If
     * the satisfaction is null, the created CachedSatisfaction label will be
     * null, irrespective of the cache policy.
     * 
     * @param satisfaction The satisfaction attached to this node
     * @param policy The policy associated with the satisfaction
     */
    public Node(@Nullable Satisfaction satisfaction, @Nullable CachePolicy policy) {
        this((satisfaction == null ? null : new CachedSatisfaction(satisfaction, policy)));
    }
    
    /**
     * Create a new Node that uses the specified label instance.
     * 
     * @param label The label instance to store with this node
     */
    public Node(@Nullable CachedSatisfaction label) {
        this.label = label;
    }
    
    /**
     * @return The current label instance of the Node, which can be null
     */
    @Nullable
    public CachedSatisfaction getLabel() {
        return label;
    }
    
    @Override
    public String toString() {
        return "Node@" + System.identityHashCode(this) + "(" + label + ")";
    }
    
    // do not override equals() and hashCode(), we want nodes to 
    // use instance equality
}
