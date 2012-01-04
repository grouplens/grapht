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

import java.util.Set;

import junit.framework.Assert;

import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Edge;
import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.graph.Node;
import org.junit.Test;

public class GraphTest {
    // FIXME: add testGetEdges() and testRemoveEdges() to test the operations
    //  that take a head and tail node, I do not think they have been tested
    
    @Test
    public void testAddNode() {
        Graph graph = new Graph();
        Node node = new MockNode();
        
        Assert.assertTrue(graph.addNode(node));
        Set<Node> nodes = graph.getNodes();
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.contains(node));
        Assert.assertTrue(graph.getOutgoingEdges(node).isEmpty());
    }
    
    @Test
    public void testUnknownNode() {
        Graph graph = new Graph();
        Node node = new MockNode();
        
        Assert.assertFalse(graph.getNodes().contains(node));
        Assert.assertNull(graph.getOutgoingEdges(node));
    }
    
    @Test
    public void testAddExistingNode() {
        Graph graph = new Graph();
        Node node = new MockNode();
        graph.addNode(node);
        
        Assert.assertFalse(graph.addNode(node));
        Set<Node> nodes = graph.getNodes();
        Assert.assertEquals(1, nodes.size());
    }
    
    @Test
    public void testRemoveNode() {
        Graph graph = new Graph();
        Node node = new MockNode();
        Node node2 = new MockNode();
        
        graph.addNode(node);
        graph.addNode(node2);
        
        Assert.assertEquals(2, graph.getNodes().size()); // sanity check
        
        Assert.assertTrue(graph.removeNode(node));
        Assert.assertEquals(1, graph.getNodes().size());
        Assert.assertTrue(graph.getNodes().contains(node2));
    }
    
    @Test
    public void testRemoveNonExistingNode() {
        Graph graph = new Graph();
        Node node = new MockNode();
        graph.addNode(new MockNode());
        
        Assert.assertFalse(graph.removeNode(node));
        Assert.assertEquals(1, graph.getNodes().size());
    }
    
    @Test
    public void testReplaceNode() {
        Graph graph = new Graph();
        
        Node toBeReplaced = new MockNode();
        Node outgoingNode = new MockNode();
        Node incomingNode = new MockNode();
        Node otherNode = new MockNode();
        
        Edge outgoing = new Edge(toBeReplaced, outgoingNode, new MockDesire());
        Edge incoming = new Edge(incomingNode, toBeReplaced, new MockDesire());
        graph.addNode(toBeReplaced);
        graph.addNode(outgoingNode);
        graph.addNode(incomingNode);
        graph.addNode(otherNode);
        graph.addEdge(outgoing);
        graph.addEdge(incoming);
        
        Node newNode = new MockNode();
        Assert.assertTrue(graph.replaceNode(toBeReplaced, newNode));
        
        // verify that the node was replaced properly
        Assert.assertEquals(4, graph.getNodes().size());
        Assert.assertTrue(graph.getNodes().contains(newNode));
        Assert.assertFalse(graph.getNodes().contains(toBeReplaced));
        
        // verify that the edges have been updated
        Assert.assertEquals(1, graph.getOutgoingEdges(newNode).size());
        Edge newOutgoing = graph.getOutgoingEdges(newNode).iterator().next();
        Assert.assertEquals(newNode, newOutgoing.getHead());
        Assert.assertEquals(outgoingNode, newOutgoing.getTail());
        Assert.assertEquals(newOutgoing, graph.getIncomingEdges(outgoingNode).iterator().next());
        
        Assert.assertEquals(1, graph.getIncomingEdges(newNode).size());
        Edge newIncoming = graph.getIncomingEdges(newNode).iterator().next();
        Assert.assertEquals(incomingNode, newIncoming.getHead());
        Assert.assertEquals(newNode, newIncoming.getTail());
        Assert.assertEquals(newIncoming, graph.getOutgoingEdges(incomingNode).iterator().next());
    }
    
    @Test
    public void testReplaceNonExistingNode() {
        Graph graph = new Graph();
        graph.addNode(new MockNode());
        
        Node toReplace = new MockNode();
        Node newNode = new MockNode();
        
        Assert.assertFalse(graph.replaceNode(toReplace, newNode));
        Assert.assertFalse(graph.getNodes().contains(newNode));
        Assert.assertFalse(graph.getNodes().contains(toReplace));
        Assert.assertEquals(1, graph.getNodes().size());
    }
    
    @Test
    public void testAddEdge() {
        Graph graph = new Graph();
        Node head = new MockNode();
        Node tail = new MockNode();
        
        Edge edge = new Edge(head, tail, new MockDesire());
        Assert.assertTrue(graph.addEdge(edge));
        
        // addEdge should auto-add the nodes too
        Assert.assertTrue(graph.getNodes().contains(head));
        Assert.assertTrue(graph.getNodes().contains(tail));
        
        Assert.assertEquals(edge, graph.getOutgoingEdges(head).iterator().next());
        Assert.assertEquals(edge, graph.getIncomingEdges(tail).iterator().next());
        
        // Add another edge to the graph
        Edge edge2 = new Edge(head, tail, new MockDesire());
        Assert.assertTrue(graph.addEdge(edge2));
        
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(edge));
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(edge2));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(edge));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(edge2));
    }
    
    @Test
    public void testAddExistingEdge() {
        Graph graph = new Graph();
        Node head = new MockNode();
        Node tail = new MockNode();
        Desire desire = new MockDesire();
        
        Edge edge = new Edge(head, tail, desire);
        graph.addEdge(edge);
        
        Assert.assertFalse(graph.addEdge(new Edge(head, tail, desire)));
    }
    
    @Test
    public void testRemoveEdge() {
        Graph graph = new Graph();
        Node head = new MockNode();
        Node tail = new MockNode();
        Desire desire = new MockDesire();
        
        Edge edge = new Edge(head, tail, desire);
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
        Graph graph = new Graph();
        Node head = new MockNode();
        Node tail = new MockNode();
        graph.addEdge(new Edge(head, tail, new MockDesire()));
        
        Edge edge = new Edge(head, tail, new MockDesire());
        Assert.assertFalse(graph.removeEdge(edge));
        Assert.assertEquals(1, graph.getOutgoingEdges(head).size());
        Assert.assertFalse(graph.getOutgoingEdges(head).contains(edge));
        Assert.assertEquals(1, graph.getIncomingEdges(tail).size());
        Assert.assertFalse(graph.getIncomingEdges(tail).contains(edge));
    }
    
    @Test
    public void testReplaceEdge() {
        Graph graph = new Graph();
        
        Node head = new MockNode();
        Node tail = new MockNode();
        
        Desire desire1 = new MockDesire();
        Desire desire2 = new MockDesire();
        
        Edge toBeReplaced = new Edge(head, tail, desire1);
        Edge otherEdge = new Edge(head, tail, new MockDesire());
        
        graph.addEdge(toBeReplaced);
        graph.addEdge(otherEdge);
        
        Edge newEdge = graph.replaceEdge(toBeReplaced, desire2);
        Assert.assertNotNull(newEdge);
        
        Assert.assertEquals(head, newEdge.getHead());
        Assert.assertEquals(tail, newEdge.getTail());
        Assert.assertEquals(desire2, newEdge.getDesire());
        
        Assert.assertFalse(graph.getOutgoingEdges(head).contains(toBeReplaced));
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(newEdge));
        Assert.assertFalse(graph.getIncomingEdges(tail).contains(toBeReplaced));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(newEdge));
    }
    
    @Test
    public void testReplaceNonExistingEdge() {
        Graph graph = new Graph();
        
        Node head = new MockNode();
        Node tail = new MockNode();
        graph.addEdge(new Edge(head, tail, new MockDesire()));
        
        Edge notInGraph = new Edge(tail, head, new MockDesire());
        Assert.assertNull(graph.replaceEdge(notInGraph, new MockDesire()));
        Assert.assertEquals(1, graph.getOutgoingEdges(head).size());
        Assert.assertEquals(1, graph.getIncomingEdges(tail).size());
        
        Assert.assertEquals(0, graph.getOutgoingEdges(tail).size());
        Assert.assertEquals(0, graph.getIncomingEdges(head).size());
    }
    
    @Test
    public void testNodeIterate() {
        Graph graph = new Graph();
        Node node1 = new MockNode();
        Node node2 = new MockNode();
        Node node3 = new MockNode();
        
        graph.addNode(node1);
        graph.addNode(node2);
        graph.addNode(node3);
        
        Set<Node> nodes = graph.getNodes();
        Assert.assertEquals(3, nodes.size());
        Assert.assertTrue(nodes.contains(node1));
        Assert.assertTrue(nodes.contains(node2));
        Assert.assertTrue(nodes.contains(node3));
    }
    
    @Test
    public void testGetIncomingEdges() {
        Graph graph = new Graph();
        
        Node tail = new MockNode();
        
        Edge edge1 = new Edge(new MockNode(), tail, new MockDesire());
        Edge edge2 = new Edge(new MockNode(), tail, new MockDesire());
        Edge edge3 = new Edge(new MockNode(), tail, new MockDesire());
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        
        Set<Edge> incoming = graph.getIncomingEdges(tail);
        Assert.assertEquals(3, incoming.size());
        Assert.assertTrue(incoming.contains(edge1));
        Assert.assertTrue(incoming.contains(edge2));
        Assert.assertTrue(incoming.contains(edge3));
    }
    
    @Test
    public void testGetOutgoingEdges() {
        Graph graph = new Graph();
        
        Node head = new MockNode();
        
        Edge edge1 = new Edge(head, new MockNode(), new MockDesire());
        Edge edge2 = new Edge(head, new MockNode(), new MockDesire());
        Edge edge3 = new Edge(head, new MockNode(), new MockDesire());
        
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);
        
        Set<Edge> outgoing = graph.getOutgoingEdges(head);
        Assert.assertEquals(3, outgoing.size());
        Assert.assertTrue(outgoing.contains(edge1));
        Assert.assertTrue(outgoing.contains(edge2));
        Assert.assertTrue(outgoing.contains(edge3));
    }
}
