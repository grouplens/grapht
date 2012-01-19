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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Graph is a utility class for composing Nodes and Desires into a usable graph,
 * using Edges to track the dependency relationships between nodes. It is a
 * fully mutable "collection" that provides methods to add, remove, and replace
 * nodes and edges. It assumes that all input nodes and edges are not null and
 * will throw exceptions otherwise. Graph is not thread safe.
 * 
 * @author Michael Ludwig
 */
public class Graph<N, E> {
    // The outgoing key set is used to represent the set of nodes in the graph,
    // although it should hold that the incoming key set is equivalent
    private final Map<Node<N>, Set<Edge<N, E>>> outgoing; // edge.head == key
    private final Map<Node<N>, Set<Edge<N, E>>> incoming; // edge.tail == key
    
    /**
     * Create an empty graph with no nodes or edges.
     */
    public Graph() {
        outgoing = new HashMap<Node<N>, Set<Edge<N, E>>>();
        incoming = new HashMap<Node<N>, Set<Edge<N, E>>>();
    }

    /**
     * Return a mutable set of nodes currently within the graph. Future changes
     * to the graph will not be reflected by the returned collection. Changes to
     * the returned set will not affect the graph.
     * 
     * @return The nodes in the graph
     */
    public Set<Node<N>> getNodes() {
        return new HashSet<Node<N>>(outgoing.keySet());
    }

    /**
     * Return the set of edges connecting the provided head and tail nodes. All
     * returned edges will have a head node equaling <tt>head</tt> and a tail
     * node equaling <tt>tail</tt>.
     * 
     * @param head The head node
     * @param tail The tail node
     * @return All edges connecting the head to the tail
     * @throws NullPointerException if head or tail are null
     */
    public Set<Edge<N, E>> getEdges(Node<N> head, Node<N> tail) {
        if (head == null || tail == null)
            throw new NullPointerException("Head and tail nodes cannot be null");
        
        Set<Edge<N, E>> outgoingEdges = outgoing.get(head);
        if (outgoingEdges != null) {
            Set<Edge<N, E>> connected = new HashSet<Edge<N, E>>();
            for (Edge<N, E> o: outgoingEdges) {
                if (o.getTail().equals(tail)) {
                    // found a connecting edge
                    connected.add(o);
                }
            }
            
            return connected;
        }
        
        // not in the graph
        return Collections.emptySet();
    }

    /**
     * <p>
     * Return a mutable set of the outgoing edges from the given node. All
     * returned edges will have a head node equaling the input node.
     * </p>
     * <p>
     * Future changes to the graph will not be reflected by the returned
     * collection. Changes to the returned set will not affect the graph. A null
     * set is returned if the node is not in the graph. An empty set is returned
     * when the node is in the graph but has no outgoing edges.
     * </p>
     * 
     * @param node The query node
     * @return All outgoing edges from the given node, or null if the node is
     *         not in the graph
     * @throws NullPointerException if node is null
     */
    public Set<Edge<N, E>> getOutgoingEdges(Node<N> node) {
        if (node == null)
            throw new NullPointerException("Node cannot be null");
        
        Set<Edge<N, E>> edges = outgoing.get(node);
        return (edges == null ? null : new HashSet<Edge<N, E>>(edges));
    }

    /**
     * <p>
     * Return a mutable set of the incoming edges to the given node. All
     * returned edges will have a tail node equaling the input node.
     * </p>
     * <p>
     * Future changes to the graph will not be reflected by the returned
     * collection. Changes to the returned set will not affect the graph. A null
     * set is returned if the node is not in the graph. An empty set is returned
     * when the node is in the graph but has no incoming edges.
     * </p>
     * 
     * @param node The query node
     * @return All incoming edges from the given node, or null if the node is
     *         not in the graph
     * @throws NullPointerException if node is null
     */
    public Set<Edge<N, E>> getIncomingEdges(Node<N> node) {
        if (node == null)
            throw new NullPointerException("Node cannot be null");
        
        Set<Edge<N, E>> edges = incoming.get(node);
        return (edges == null ? null : new HashSet<Edge<N, E>>(edges));
    }

    /**
     * Add the given Node to this graph. If the node is already in the graph,
     * false is returned and the graph is not modified. Otherwise, true is
     * returned and the node will be added with no incoming or outgoing edges.
     * 
     * @param node The node to add
     * @return True if the graph was modified as a result of this method call
     * @throws NullPointerException if node is null
     */
    public boolean addNode(Node<N> node) {
        if (node == null)
            throw new NullPointerException("Node cannot be null");
        
        if (!outgoing.containsKey(node)) {
            // add the node, with 0 outgoing and incoming edges
            outgoing.put(node, new HashSet<Edge<N, E>>());
            incoming.put(node, new HashSet<Edge<N, E>>());
            
            return true;
        } else
            return false;
    }

    /**
     * Remove the given Node from this graph. All incoming and outgoing edges
     * connected to the node will be removed as well. If the node is not in the
     * graph or was already removed, false is returned and the graph will have
     * not been modified.
     * 
     * @param node The node to remove
     * @return True if the graph was modified as a result of this method call
     * @throws NullPointerException if node is null
     */
    public boolean removeNode(Node<N> node) {
        if (node == null)
            throw new NullPointerException("Node cannot be null");
        
        // remove the node and all outgoing edges from graph
        Set<Edge<N, E>> outgoingEdges = outgoing.remove(node);
        if (outgoingEdges != null) {
            // remove outgoing edges from incoming graph as well
            for (Edge<N, E> o: outgoingEdges)
                incoming.get(o.getTail()).remove(o);
            
            // remove the node and all incoming edges from the graph
            // - here we assume it was in the graph
            Set<Edge<N, E>> incomingEdges = incoming.remove(node);
            for (Edge<N, E> i: incomingEdges)
                outgoing.get(i.getHead()).remove(i);
            
            return true;
        } else
            return false;
    }

    /**
     * <p>
     * Update the structure of this graph so that all occurrences of
     * <tt>oldNode</tt> are replaced with <tt>newNode</tt>. All edges
     * originating from the old node will be replaced with new edges originating
     * from the new node, but sharing the original tail node and desire. All
     * edges incoming to the old node will be replaced with edges sharing the
     * original head node and desire, but ending at the new node.
     * </p>
     * <p>
     * If oldNode is not in the graph, then this does nothing and the graph is
     * not modified.
     * </p>
     * 
     * @param oldNode The old node that will be replaced
     * @param newNode The new node that will be added in place of oldNode
     * @return True if the graph was modified as a result of this method call
     * @throws NullPointerException if oldNode or newNode is null
     */
    public boolean replaceNode(Node<N> oldNode, Node<N> newNode) {
        if (oldNode == null || newNode == null)
            throw new NullPointerException("Nodes cannot be null");
        
        Set<Edge<N, E>> oldOutgoingEdges = outgoing.remove(oldNode);
        if (oldOutgoingEdges != null) {
            // create new edges from the new node to the original tail node,
            // and remove old outgoing edges from incoming graph
            Set<Edge<N, E>> newOutgoingEdges = new HashSet<Edge<N, E>>();
            for (Edge<N, E> old: oldOutgoingEdges) {
                Edge<N, E> newEdge = new Edge<N, E>(newNode, old.getTail(), old.getPayload());
                newOutgoingEdges.add(newEdge);
                incoming.get(old.getTail()).remove(old);
                incoming.get(old.getTail()).add(newEdge);
            }
            
            // add new outgoing edges
            outgoing.put(newNode, newOutgoingEdges);
            
            
            // like with removeNode() we assume incoming contains the node now
            Set<Edge<N, E>> oldIncomingEdges = incoming.remove(oldNode);
            // create new edges from the original tail node to the new node
            // and remove old incoming edges from the outgoing graph
            Set<Edge<N, E>> newIncomingEdges = new HashSet<Edge<N, E>>();
            for (Edge<N, E> old: oldIncomingEdges) {
                Edge<N, E> newEdge = new Edge<N, E>(old.getHead(), newNode, old.getPayload());
                newIncomingEdges.add(newEdge);
                outgoing.get(old.getHead()).remove(old);
                outgoing.get(old.getHead()).add(newEdge);
            }
            
            // add new incoming edges
            incoming.put(newNode, newIncomingEdges);
            return true;
        } else {
            // oldNode was not in this graph, so do nothing
            return false;
        }
    }

    /**
     * Add the provided edge to this graph. If need be, the head and tail nodes
     * of the edge are also added to the graph. False is returned if the edge is
     * already in the graph. True is returned if adding the edge modified the
     * graph's state. It is possible to add multiple edges between the same head
     * and tail nodes as long as the Edge instances are different.
     * 
     * @param edge The edge to add
     * @return True if the graph was modified as a result of this method call
     * @throws NullPointerException if edge is null
     */
    public boolean addEdge(Edge<N, E> edge) {
        if (edge == null)
            throw new NullPointerException("Edge cannot be null");
        
        // add the head and tail to the graph - this does nothing if they're already
        // in the graph, so it's safe
        addNode(edge.getHead());
        addNode(edge.getTail());
        
        if (outgoing.get(edge.getHead()).add(edge)) {
            incoming.get(edge.getTail()).add(edge);
            return true;
        } else {
            // already in the graph
            return false;
        }
    }

    /**
     * Remove the edge from this graph. The nodes of the edge are left in the
     * graph, even if either has no more incoming or outgoing edges. False is
     * returned if the edge is not in this graph.
     * 
     * @param edge The edge to remove
     * @return True if the graph was modified as a result of this method call
     * @throws NullPointerException if edge is null
     */
    public boolean removeEdge(Edge<N, E> edge) {
        if (edge == null)
            throw new NullPointerException("Edge cannot be null");
        
        Set<Edge<N, E>> outgoingEdges = outgoing.get(edge.getHead());
        if (outgoingEdges != null) {
            // remove the edge from both outgoing and incoming
            if (outgoingEdges.remove(edge)) {
                incoming.get(edge.getTail()).remove(edge);
                return true;
            }
        }
        
        // edge was not in the graph
        return false;
    }

    /**
     * Remove and return a the set of all edges in this graph connecting the given head and
     * tail nodes. Edges connecting the nodes in the direction of the tail to
     * the head are not removed. If there are no such edges, the empty set is returned. If
     * there are multiple connecting edges, all such edges are removed and returned.
     * 
     * @param head The head of the edge
     * @param tail The tail of the edge
     * @return The edges that were removed
     * @throws NullPointerException if head or tail are null
     */
    public Set<Edge<N, E>> removeEdges(Node<N> head, Node<N> tail) {
        if (head == null || tail == null)
            throw new NullPointerException("Head and tail nodes cannot be null");
        
        Set<Edge<N, E>> outgoingEdges = outgoing.get(head);
        if (outgoingEdges != null) {
            Set<Edge<N, E>> removed = new HashSet<Edge<N, E>>();

            // clone set so we can call removeEdge() without ConcurrentModificationExceptions
            outgoingEdges = new HashSet<Edge<N, E>>(outgoingEdges);
            for (Edge<N, E> o: outgoingEdges) {
                if (o.getTail().equals(tail)) {
                    // found the edge so remove it
                    removeEdge(o);
                    removed.add(o);
                }
            }
            
            return removed;
        }
        
        // if we've gotten here, no edge existed 
        return Collections.emptySet();
    }

    /**
     * <p>
     * Update the structure of this graph so that the provided old edge is
     * replaced by a new edge that is created with the same head and tail nodes,
     * but has the new payload. The set of outgoing edges from the head node
     * will be updated to no longer include the old edge and to include the new
     * edge with the new payload. The set of incoming edges to the tail node
     * will be similarly updated.
     * </p>
     * <p>
     * The newly created and added edge will be returned. Its head and tail will
     * equal the head and tail of <tt>oldEdge</tt> and its payload will be
     * <tt>newPayload</tt>. If the old edge is not in this graph, then no new
     * edge is added and null is returned.
     * </p>
     * 
     * @param oldEdge The old edge to remove and replace
     * @param newPayload The new payload for the new edge that is replacing
     *            oldEdge
     * @return The new edge, or null if oldEdge was not in this graph
     * @throws NullPointerException if oldEdge or newPayload are null
     */
    public Edge<N, E> replaceEdge(Edge<N, E> oldEdge, E newPayload) {
        if (oldEdge == null)
            throw new NullPointerException("Old edge cannot be null");
        if (newPayload == null)
            throw new NullPointerException("New payload cannot be null");
        
        Set<Edge<N, E>> outgoingEdges = outgoing.get(oldEdge.getHead());
        if (outgoingEdges != null) {
            if (outgoingEdges.remove(oldEdge)) {
                // the old edge was in the graph so replace it
                Edge<N, E> newEdge = new Edge<N, E>(oldEdge.getHead(), oldEdge.getTail(), newPayload);
                outgoingEdges.add(newEdge);
                
                // now we replace the incoming edge as well, but we assume it exists
                Set<Edge<N, E>> incomingEdges = incoming.get(oldEdge.getTail());
                incomingEdges.remove(oldEdge);
                incomingEdges.add(newEdge);
                
                return newEdge;
            }
        }
        
        // if we've gotten here, the old edge was not in the graph
        return null;
    }
}
