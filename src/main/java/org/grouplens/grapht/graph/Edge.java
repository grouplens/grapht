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

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.util.FrozenList;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * Edge is an immutable, unidirectional connection between two Nodes, the head
 * and the tail. An Edge is used when representing a directional graph,
 * connecting any two nodes within the graph. Thus, an edge from X to Y is
 * different from an edge from Y to X. Additionally, the edge stores a
 * desire representing the goal linking the head and tail.
 * node.
 * <p>
 * Edges use instance equality, regardless of how their labels might implement
 * equality or how their attached Nodes. Thus, if two Edges instances in a graph
 * have the same label and nodes, they are still considered separate edges
 * from the graph's perspective. In those situations, the graph has multiple
 * edges from one node to the other.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class Edge implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private final Node head;
    private final Node tail;
    private final FrozenList<Desire> desires;

    /**
     * Create a new Edge between the two Nodes, source'ed at <tt>head</tt> and
     * ending at <tt>tail</tt>. The provided list of Desires is the sequence of Desires
     * followed by binding functions to reach the tail with the edge between the
     * two nodes.
     * 
     * @param head The start or head node of the edge
     * @param tail The end or tail node of the edge
     * @param label The label data along the edge
     * @throws NullPointerException if the head, tail
     */
    public Edge(Node head, Node tail, @Nullable List<Desire> label) {
        if (head == null || tail == null)
            throw new NullPointerException("Head and tail cannot be null");
        
        this.head = head;
        this.tail = tail;
        
        // this serves three purposes:
        // 1. clone input so original list can be modified
        // 2. guarantee our field is serializable
        // 3. make the returned instance unmodifiable
        if (label != null) {
            this.desires = new FrozenList<Desire>(label);
        } else {
            this.desires = null;
        }
    }

    /**
     * Get the head node of this edge. Edges are uni-directional, so the edge is
     * source'ed at the head and connects to the tail.
     * 
     * @return The head node of the edge
     */
    public Node getHead() {
        return head;
    }

    /**
     * Get the tail node of this edge. Edges are uni-directional, so the edge
     * ends at the tail and comes from the head.
     * 
     * @return The tail node of the edge
     */
    public Node getTail() {
        return tail;
    }

    /**
     * Get the chain of desires associated with this edge. This may be null if no
     * desire was assigned to the edge.
     * 
     * @return The chain of desires on this edge
     * @deprecated  Use {@link #getDesireChain}.
     */
    @Nullable
    @Deprecated
    public List<Desire> getLabel() {
        return desires;
    }
    
    /**
     * Return the chain of Desire of this edge's label.  The first Desire in the 
     * chain is the one that applies directly to this edge.
     * If the sequence is null or empty, a null list is returned.
     * 
     * @return The first or primary desire along this edge
     */
    @Nullable
    public List<Desire> getDesireChain() {
        return desires;
    }
    

    /**
     * Return the first Desire in the sequence of desires of this edge's label.
     * If the sequence is null or empty, a null desire is returned.
     * 
     * @return The first or primary desire along this edge
     */
    @Nullable
    public Desire getDesire() {
        return (desires == null || desires.isEmpty() ? null : desires.get(0));
    }
    
    @Override
    public String toString() {
        return "Edge(head=" + head + ", tail=" + tail + ", label=" + desires + ")";
    }
    
    // do not override equals() or hashCode() because edges use instance equality
}
