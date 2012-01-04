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
package org.grouplens.inject.graph;

/**
 * <p>
 * Edge is an immutable, unidirectional connection between two Nodes, the head
 * and the tail. An Edge is used when representing a directional graph,
 * connecting any two nodes within the graph. Thus, an edge from X to Y is
 * different from an edge from Y to X. Additionally, the edge stores a
 * domain-specific payload representing the relationship between the head and
 * tail node.
 * <p>
 * Edges use instance equality, regardless of how their payloads might implement
 * equality or how their attached Nodes. Thus, if two Edges instances in a graph
 * have the same payload and nodes, they are still considered separate nodes
 * from the graph's perspective. In those situations, the graph has multiple
 * edges from one node to the other.
 * 
 * @author Michael Ludwig
 */
public class Edge<N, E> {
    private final Node<N> head;
    private final Node<N> tail;
    
    private final E payload;

    /**
     * Create a new Edge between the two Nodes, source'ed at <tt>head</tt> and
     * ending at <tt>tail</tt>. The provided payload is a domain-specific
     * object to be associated with the edge between the two nodes.
     * 
     * @param head The start or head node of the edge
     * @param tail The end or tail node of the edge
     * @param payload The payload data along the edge
     * @throws NullPointerException if the head, tail, or payload are null
     */
    public Edge(Node<N> head, Node<N> tail, E payload) {
        if (head == null || tail == null || payload == null)
            throw new NullPointerException("Arguments cannot be null");
        
        this.head = head;
        this.tail = tail;
        this.payload = payload;
    }

    /**
     * Get the head node of this edge. Edges are uni-directional, so the edge is
     * source'ed at the head and connects to the tail.
     * 
     * @return The head node of the edge
     */
    public Node<N> getHead() {
        return head;
    }

    /**
     * Get the tail node of this edge. Edges are uni-directional, so the edge
     * ends at the tail and comes from the head.
     * 
     * @return The tail node of the edge
     */
    public Node<N> getTail() {
        return tail;
    }

    /**
     * Get the payload associated with this edge. This may be null if no
     * payload was assigned to the edge
     * 
     * @return The payload on this edge
     */
    public E getPayload() {
        return payload;
    }
    
    // do not override equals() or hashCode() because edges use instance equality
}
