/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.graph;


/**
 * <p>
 * A Node represents a conceptual node within a graph. Nodes within the same
 * graph can contain payload instances that represent domain-specific data for
 * the graph.
 * <p>
 * Nodes use instance equality, regardless of how their payloads might implement
 * equality or how they compare. Thus, if two Node instances in a graph have the
 * same payload, they are still considered separate nodes from the graph's
 * perspective.
 * 
 * @see Edge
 * @see Graph
 * @author Michael Ludwig <mludwig@cs.umn.edu
 */
public class Node<T> {
    private final T payload;

    /**
     * Create a new Node that uses the specified payload instance.
     * 
     * @param payload The payload instance to store with this node
     * @throws NullPointerException if payload is null
     */
    public Node(T payload) {
        if (payload == null)
            throw new NullPointerException("Payload cannot be null");
        this.payload = payload;
    }
    
    /**
     * @return The current payload instance of the Node
     */
    public T getPayload() {
        return payload;
    }
    
    @Override
    public String toString() {
        return "Node@" + System.identityHashCode(this) + "(" + payload + ")";
    }
    
    // do not override equals() and hashCode(), we want nodes to 
    // use instance equality
}
