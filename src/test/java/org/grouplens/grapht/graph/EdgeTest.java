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

import org.junit.Assert;
import org.grouplens.grapht.spi.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EdgeTest {
    @Test
    public void testGetters() {
        Node head = new Node(new CachedSatisfaction(new MockSatisfaction(), CachePolicy.MEMOIZE));
        Node tail = new Node(new CachedSatisfaction(new MockSatisfaction(), CachePolicy.MEMOIZE));
        List<Desire> ep = Arrays.<Desire>asList(new MockDesire());
        
        Edge edge = new Edge(head, tail, ep);
        
        Assert.assertEquals(head, edge.getHead());
        Assert.assertEquals(tail, edge.getTail());
        Assert.assertEquals(ep, edge.getDesireChain());
    }
    
    @Test
    public void testEquals() {
        Node head = new Node(new CachedSatisfaction(new MockSatisfaction(), CachePolicy.MEMOIZE));
        Node tail = new Node(new CachedSatisfaction(new MockSatisfaction(), CachePolicy.MEMOIZE));
        List<Desire> ep1 = Arrays.<Desire>asList(new MockDesire());
        List<Desire> ep2 = Arrays.<Desire>asList(new MockDesire());

        Edge edge1 = new Edge(head, tail, ep1);
        Edge edge2 = new Edge(head, tail, ep1);
        Edge reversedEdge = new Edge(tail, head, ep1);
        Edge diffPayload = new Edge(head, tail, ep2);
        
        Assert.assertEquals(edge1, edge1);
        // Edges use instance equality
        Assert.assertFalse(edge1.equals(edge2));
        Assert.assertFalse(edge1.equals(reversedEdge));
        Assert.assertFalse(edge1.equals(diffPayload));
    }
    
    @Test
    public void testHashcode() {
        Node head = new Node(new CachedSatisfaction(new MockSatisfaction(), CachePolicy.MEMOIZE));
        Node tail = new Node(new CachedSatisfaction(new MockSatisfaction(), CachePolicy.MEMOIZE));
        List<Desire> ep = Arrays.<Desire>asList(new MockDesire());
        
        Edge edge1 = new Edge(head, tail, ep);
        Edge edge2 = new Edge(head, tail, ep);
        
        Set<Edge> edges = new HashSet<Edge>();
        edges.add(edge1);
        
        // Edges use instance equality and hashing, so make sure that edge2
        // doesn't appear in the set
        Assert.assertTrue(edges.contains(edge1));
        Assert.assertFalse(edges.contains(edge2));
    }
}
