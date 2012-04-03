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

import java.util.Set;

import junit.framework.Assert;

import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.junit.Test;

public class GraphTest {
    @Test
    public void testGetEdges() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        
        Edge<Object, Object> edge1 = new Edge<Object, Object>(head, tail, new Object());
        Edge<Object, Object> edge2 = new Edge<Object, Object>(head, tail, new Object());
        Edge<Object, Object> edge3 = new Edge<Object, Object>(tail, head, new Object());
        
        graph.addNode(head);
        graph.addNode(tail);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);

        Set<Edge<Object, Object>> edges = graph.getEdges(head, tail);
        Assert.assertEquals(2, edges.size());
        Assert.assertTrue(edges.contains(edge1));
        Assert.assertTrue(edges.contains(edge2));
        Assert.assertFalse(edges.contains(edge3));
    }
    
    @Test
    public void testRemoveEdges() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        
        Edge<Object, Object> edge1 = new Edge<Object, Object>(head, tail, new Object());
        Edge<Object, Object> edge2 = new Edge<Object, Object>(head, tail, new Object());
        Edge<Object, Object> edge3 = new Edge<Object, Object>(tail, head, new Object());
        
        graph.addNode(head);
        graph.addNode(tail);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);

        Set<Edge<Object, Object>> edges = graph.removeEdges(head, tail);
        Assert.assertEquals(2, edges.size());
        Assert.assertTrue(edges.contains(edge1));
        Assert.assertTrue(edges.contains(edge2));
        Assert.assertFalse(edges.contains(edge3));
        
        Assert.assertTrue(graph.getOutgoingEdges(tail).contains(edge3));
    }
    
    @Test
    public void testAddNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> node = new Node<Object>(new Object());
        
        Assert.assertTrue(graph.addNode(node));
        Set<Node<Object>> nodes = graph.getNodes();
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.contains(node));
        Assert.assertTrue(graph.getOutgoingEdges(node).isEmpty());
    }
    
    @Test
    public void testUnknownNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> node = new Node<Object>(new Object());
        
        Assert.assertFalse(graph.getNodes().contains(node));
        Assert.assertNull(graph.getOutgoingEdges(node));
    }
    
    @Test
    public void testAddExistingNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> node = new Node<Object>(new Object());
        graph.addNode(node);
        
        Assert.assertFalse(graph.addNode(node));
        Set<Node<Object>> nodes = graph.getNodes();
        Assert.assertEquals(1, nodes.size());
    }
    
    @Test
    public void testRemoveNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> node = new Node<Object>(new Object());
        Node<Object> node2 = new Node<Object>(new Object());
        
        graph.addNode(node);
        graph.addNode(node2);
        
        Assert.assertEquals(2, graph.getNodes().size()); // sanity check
        
        Assert.assertTrue(graph.removeNode(node));
        Assert.assertEquals(1, graph.getNodes().size());
        Assert.assertTrue(graph.getNodes().contains(node2));
    }
    
    @Test
    public void testRemoveNonExistingNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> node = new Node<Object>(new Object());
        graph.addNode(new Node<Object>(new Object()));
        
        Assert.assertFalse(graph.removeNode(node));
        Assert.assertEquals(1, graph.getNodes().size());
    }
    
    @Test
    public void testReplaceNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        
        Node<Object> toBeReplaced = new Node<Object>(new Object());
        Node<Object> outgoingNode = new Node<Object>(new Object());
        Node<Object> incomingNode = new Node<Object>(new Object());
        Node<Object> otherNode = new Node<Object>(new Object());
        
        Edge<Object, Object> outgoing = new Edge<Object, Object>(toBeReplaced, outgoingNode, new Object());
        Edge<Object, Object> incoming = new Edge<Object, Object>(incomingNode, toBeReplaced, new Object());
        graph.addNode(toBeReplaced);
        graph.addNode(outgoingNode);
        graph.addNode(incomingNode);
        graph.addNode(otherNode);
        graph.addEdge(outgoing);
        graph.addEdge(incoming);
        
        Node<Object> newNode = new Node<Object>(new Object());
        Assert.assertTrue(graph.replaceNode(toBeReplaced, newNode));
        
        // verify that the node was replaced properly
        Assert.assertEquals(4, graph.getNodes().size());
        Assert.assertTrue(graph.getNodes().contains(newNode));
        Assert.assertFalse(graph.getNodes().contains(toBeReplaced));
        
        // verify that the edges have been updated
        Assert.assertEquals(1, graph.getOutgoingEdges(newNode).size());
        Edge<Object, Object> newOutgoing = graph.getOutgoingEdges(newNode).iterator().next();
        Assert.assertEquals(newNode, newOutgoing.getHead());
        Assert.assertEquals(outgoingNode, newOutgoing.getTail());
        Assert.assertEquals(newOutgoing, graph.getIncomingEdges(outgoingNode).iterator().next());
        
        Assert.assertEquals(1, graph.getIncomingEdges(newNode).size());
        Edge<Object, Object> newIncoming = graph.getIncomingEdges(newNode).iterator().next();
        Assert.assertEquals(incomingNode, newIncoming.getHead());
        Assert.assertEquals(newNode, newIncoming.getTail());
        Assert.assertEquals(newIncoming, graph.getOutgoingEdges(incomingNode).iterator().next());
    }
    
    @Test
    public void testReplaceNonExistingNode() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        graph.addNode(new Node<Object>(new Object()));
        
        Node<Object> toReplace = new Node<Object>(new Object());
        Node<Object> newNode = new Node<Object>(new Object());
        
        Assert.assertFalse(graph.replaceNode(toReplace, newNode));
        Assert.assertFalse(graph.getNodes().contains(newNode));
        Assert.assertFalse(graph.getNodes().contains(toReplace));
        Assert.assertEquals(1, graph.getNodes().size());
    }
    
    @Test
    public void testAddEdge() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        
        Edge<Object, Object> edge = new Edge<Object, Object>(head, tail, new Object());
        Assert.assertTrue(graph.addEdge(edge));
        
        // addEdge should auto-add the nodes too
        Assert.assertTrue(graph.getNodes().contains(head));
        Assert.assertTrue(graph.getNodes().contains(tail));
        
        Assert.assertEquals(edge, graph.getOutgoingEdges(head).iterator().next());
        Assert.assertEquals(edge, graph.getIncomingEdges(tail).iterator().next());
        
        // Add another edge to the graph
        Edge<Object, Object> edge2 = new Edge<Object, Object>(head, tail, new Object());
        Assert.assertTrue(graph.addEdge(edge2));
        
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(edge));
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(edge2));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(edge));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(edge2));
    }
    
    @Test
    public void testAddExistingEdge() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        Object payload = new Object();
        
        Edge<Object, Object> edge = new Edge<Object, Object>(head, tail, payload);
        graph.addEdge(edge);
        
        Assert.assertFalse(graph.addEdge(edge));
    }
    
    @Test
    public void testRemoveEdge() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        Object payload = new Object();
        
        Edge<Object, Object> edge = new Edge<Object, Object>(head, tail, payload);
        graph.addEdge(edge);
        
        Assert.assertTrue(graph.removeEdge(edge));
        
        // make sure the edge is gone
        Assert.assertEquals(0, graph.getOutgoingEdges(head).size());
        Assert.assertEquals(0, graph.getIncomingEdges(tail).size());
        
        // make sure the nodes didn't get removed
        Assert.assertTrue(graph.getNodes().contains(head));
        Assert.assertTrue(graph.getNodes().contains(tail));
    }
    
    @Test
    public void testRemoveNonExistingEdge() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        graph.addEdge(new Edge<Object, Object>(head, tail, new Object()));
        
        Edge<Object, Object> edge = new Edge<Object, Object>(head, tail, new Object());
        Assert.assertFalse(graph.removeEdge(edge));
        Assert.assertEquals(1, graph.getOutgoingEdges(head).size());
        Assert.assertFalse(graph.getOutgoingEdges(head).contains(edge));
        Assert.assertEquals(1, graph.getIncomingEdges(tail).size());
        Assert.assertFalse(graph.getIncomingEdges(tail).contains(edge));
    }
    
    @Test
    public void testReplaceEdge() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        
        Object payload1 = new Object();
        Object payload2 = new Object();
        
        Edge<Object, Object> toBeReplaced = new Edge<Object, Object>(head, tail, payload1);
        Edge<Object, Object> otherEdge = new Edge<Object, Object>(head, tail, new Object());
        
        graph.addEdge(toBeReplaced);
        graph.addEdge(otherEdge);
        
        Edge<Object, Object> newEdge = graph.updateEdgeLabel(toBeReplaced, payload2);
        Assert.assertNotNull(newEdge);
        
        Assert.assertEquals(head, newEdge.getHead());
        Assert.assertEquals(tail, newEdge.getTail());
        Assert.assertEquals(payload2, newEdge.getLabel());
        
        Assert.assertFalse(graph.getOutgoingEdges(head).contains(toBeReplaced));
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(newEdge));
        Assert.assertFalse(graph.getIncomingEdges(tail).contains(toBeReplaced));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(newEdge));
    }
    
    @Test
    public void testReplaceNonExistingEdge() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        graph.addEdge(new Edge<Object, Object>(head, tail, new Object()));
        
        Edge<Object, Object> notInGraph = new Edge<Object, Object>(tail, head, new Object());
        Assert.assertNull(graph.updateEdgeLabel(notInGraph, new Object()));
        Assert.assertEquals(1, graph.getOutgoingEdges(head).size());
        Assert.assertEquals(1, graph.getIncomingEdges(tail).size());
        
        Assert.assertEquals(0, graph.getOutgoingEdges(tail).size());
        Assert.assertEquals(0, graph.getIncomingEdges(head).size());
    }
    
    @Test
    public void testNodeIterate() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        Node<Object> node1 = new Node<Object>(new Object());
        Node<Object> node2 = new Node<Object>(new Object());
        Node<Object> node3 = new Node<Object>(new Object());
        
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        
        Set<Node<Object>> nodes = graph.getNodes();
        Assert.assertEquals(3, nodes.size());
        Assert.assertTrue(nodes.contains(node1));
        Assert.assertTrue(nodes.contains(node2));
        Assert.assertTrue(nodes.contains(node3));
    }
    
    @Test
    public void testGetIncomingEdges() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        
        Node<Object> tail = new Node<Object>(new Object());
        
        Edge<Object, Object> edge1 = new Edge<Object, Object>(new Node<Object>(new Object()), tail, new Object());
        Edge<Object, Object> edge2 = new Edge<Object, Object>(new Node<Object>(new Object()), tail, new Object());
        Edge<Object, Object> edge3 = new Edge<Object, Object>(new Node<Object>(new Object()), tail, new Object());
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        
        Set<Edge<Object, Object>> incoming = graph.getIncomingEdges(tail);
        Assert.assertEquals(3, incoming.size());
        Assert.assertTrue(incoming.contains(edge1));
        Assert.assertTrue(incoming.contains(edge2));
        Assert.assertTrue(incoming.contains(edge3));
    }
    
    @Test
    public void testGetOutgoingEdges() {
        Graph<Object, Object> graph = new Graph<Object, Object>();
        
        Node<Object> head = new Node<Object>(new Object());
        
        Edge<Object, Object> edge1 = new Edge<Object, Object>(head, new Node<Object>(new Object()), new Object());
        Edge<Object, Object> edge2 = new Edge<Object, Object>(head, new Node<Object>(new Object()), new Object());
        Edge<Object, Object> edge3 = new Edge<Object, Object>(head, new Node<Object>(new Object()), new Object());
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        
        Set<Edge<Object, Object>> outgoing = graph.getOutgoingEdges(head);
        Assert.assertEquals(3, outgoing.size());
        Assert.assertTrue(outgoing.contains(edge1));
        Assert.assertTrue(outgoing.contains(edge2));
        Assert.assertTrue(outgoing.contains(edge3));
    }
}
