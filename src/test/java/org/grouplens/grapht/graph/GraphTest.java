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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.junit.Test;

public class GraphTest {
    @Test
    public void testGetEdges() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge1 = new Edge(head, tail, null);
        Edge edge2 = new Edge(head, tail, null);
        Edge edge3 = new Edge(tail, head, null);
        
        graph.addNode(head);
        graph.addNode(tail);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);

        Set<Edge> edges = graph.getEdges(head, tail);
        Assert.assertEquals(2, edges.size());
        Assert.assertTrue(edges.contains(edge1));
        Assert.assertTrue(edges.contains(edge2));
        Assert.assertFalse(edges.contains(edge3));
    }
    
    @Test
    public void testRemoveEdges() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge1 = new Edge(head, tail, null);
        Edge edge2 = new Edge(head, tail, null);
        Edge edge3 = new Edge(tail, head, null);
        
        graph.addNode(head);
        graph.addNode(tail);
        graph.addEdge(edge1);
        graph.addEdge(edge2);
        graph.addEdge(edge3);

        Set<Edge> edges = graph.removeEdges(head, tail);
        Assert.assertEquals(2, edges.size());
        Assert.assertTrue(edges.contains(edge1));
        Assert.assertTrue(edges.contains(edge2));
        Assert.assertFalse(edges.contains(edge3));
        
        Assert.assertTrue(graph.getOutgoingEdges(tail).contains(edge3));
    }
    
    @Test
    public void testAddNode() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node node = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Assert.assertTrue(graph.addNode(node));
        Set<Node> nodes = graph.getNodes();
        Assert.assertEquals(1, nodes.size());
        Assert.assertTrue(nodes.contains(node));
        Assert.assertTrue(graph.getOutgoingEdges(node).isEmpty());
    }
    
    @Test
    public void testUnknownNode() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node node = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Assert.assertFalse(graph.getNodes().contains(node));
        Assert.assertNull(graph.getOutgoingEdges(node));
    }
    
    @Test
    public void testAddExistingNode() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node node = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        graph.addNode(node);
        
        Assert.assertFalse(graph.addNode(node));
        Set<Node> nodes = graph.getNodes();
        Assert.assertEquals(1, nodes.size());
    }
    
    @Test
    public void testRemoveNode() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node node = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node node2 = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        graph.addNode(node);
        graph.addNode(node2);
        
        Assert.assertEquals(2, graph.getNodes().size()); // sanity check
        
        Assert.assertTrue(graph.removeNode(node));
        Assert.assertEquals(1, graph.getNodes().size());
        Assert.assertTrue(graph.getNodes().contains(node2));
    }
    
    @Test
    public void testRemoveNonExistingNode() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node node = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        graph.addNode(new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)));
        
        Assert.assertFalse(graph.removeNode(node));
        Assert.assertEquals(1, graph.getNodes().size());
    }
    
    @Test
    public void testReplaceNode() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        
        Node toBeReplaced = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node outgoingNode = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node incomingNode = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node otherNode = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge outgoing = new Edge(toBeReplaced, outgoingNode, null);
        Edge incoming = new Edge(incomingNode, toBeReplaced, null);
        graph.addNode(toBeReplaced);
        graph.addNode(outgoingNode);
        graph.addNode(incomingNode);
        graph.addNode(otherNode);
        graph.addEdge(outgoing);
        graph.addEdge(incoming);
        
        Node newNode = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
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
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        graph.addNode(new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)));
        
        Node toReplace = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node newNode = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Assert.assertFalse(graph.replaceNode(toReplace, newNode));
        Assert.assertFalse(graph.getNodes().contains(newNode));
        Assert.assertFalse(graph.getNodes().contains(toReplace));
        Assert.assertEquals(1, graph.getNodes().size());
    }
    
    @Test
    public void testAddEdge() {
        Graph graph = new Graph();
        InjectSPI spi = new ReflectionInjectSPI();
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge = new Edge(head, tail, null);
        Assert.assertTrue(graph.addEdge(edge));
        
        // addEdge should auto-add the nodes too
        Assert.assertTrue(graph.getNodes().contains(head));
        Assert.assertTrue(graph.getNodes().contains(tail));
        
        Assert.assertEquals(edge, graph.getOutgoingEdges(head).iterator().next());
        Assert.assertEquals(edge, graph.getIncomingEdges(tail).iterator().next());
        
        // Add another edge to the graph
        Edge edge2 = new Edge(head, tail, null);
        Assert.assertTrue(graph.addEdge(edge2));
        
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(edge));
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(edge2));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(edge));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(edge2));
    }
    
    @Test
    public void testAddExistingEdge() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge = new Edge(head, tail, null);
        graph.addEdge(edge);
        
        Assert.assertFalse(graph.addEdge(edge));
    }
    
    @Test
    public void testRemoveEdge() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge = new Edge(head, tail, null);
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
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        graph.addEdge(new Edge(head, tail, null));
        
        Edge edge = new Edge(head, tail, null);
        Assert.assertFalse(graph.removeEdge(edge));
        Assert.assertEquals(1, graph.getOutgoingEdges(head).size());
        Assert.assertFalse(graph.getOutgoingEdges(head).contains(edge));
        Assert.assertEquals(1, graph.getIncomingEdges(tail).size());
        Assert.assertFalse(graph.getIncomingEdges(tail).contains(edge));
    }
    
    @Test
    public void testReplaceEdge() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        List<Desire> payload1 = Arrays.asList(spi.desire(null, Object.class, false));
        List<Desire> payload2 = Arrays.asList(spi.desire(null, Object.class, true));
        
        Edge toBeReplaced = new Edge(head, tail, payload1);
        Edge otherEdge = new Edge(head, tail, null);
        
        graph.addEdge(toBeReplaced);
        graph.addEdge(otherEdge);
        
        Edge newEdge = graph.updateEdgeLabel(toBeReplaced, payload2);
        Assert.assertNotNull(newEdge);
        
        Assert.assertEquals(head, newEdge.getHead());
        Assert.assertEquals(tail, newEdge.getTail());
        Assert.assertEquals(payload2, newEdge.getDesireChain());
        
        Assert.assertFalse(graph.getOutgoingEdges(head).contains(toBeReplaced));
        Assert.assertTrue(graph.getOutgoingEdges(head).contains(newEdge));
        Assert.assertFalse(graph.getIncomingEdges(tail).contains(toBeReplaced));
        Assert.assertTrue(graph.getIncomingEdges(tail).contains(newEdge));
    }
    
    @Test
    public void testReplaceNonExistingEdge() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        graph.addEdge(new Edge(head, tail, null));
        
        Edge notInGraph = new Edge(tail, head, null);
        Assert.assertNull(graph.updateEdgeLabel(notInGraph, null));
        Assert.assertEquals(1, graph.getOutgoingEdges(head).size());
        Assert.assertEquals(1, graph.getIncomingEdges(tail).size());
        
        Assert.assertEquals(0, graph.getOutgoingEdges(tail).size());
        Assert.assertEquals(0, graph.getIncomingEdges(head).size());
    }
    
    @Test
    public void testNodeIterate() {
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        Node node1 = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node node2 = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        Node node3 = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
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
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        
        Node tail = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge1 = new Edge(new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)), tail, null);
        Edge edge2 = new Edge(new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)), tail, null);
        Edge edge3 = new Edge(new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)), tail, null);
        
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
        InjectSPI spi = new ReflectionInjectSPI();
        Graph graph = new Graph();
        
        Node head = new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE));
        
        Edge edge1 = new Edge(head, new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)), null);
        Edge edge2 = new Edge(head, new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)), null);
        Edge edge3 = new Edge(head, new Node(new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE)), null);
        
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
