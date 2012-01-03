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

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Edge;
import org.grouplens.inject.graph.Node;
import org.junit.Test;

public class EdgeTest {
    @Test
    public void testGetters() {
        Node head = new MockNode();
        Node tail = new MockNode();
        Desire desire = new MockDesire();
        
        Edge edge = new Edge(head, tail, desire);
        
        Assert.assertEquals(head, edge.getHead());
        Assert.assertEquals(tail, edge.getTail());
        Assert.assertEquals(desire, edge.getDesire());
    }
    
    @Test
    public void testEquals() {
        Node head = new MockNode();
        Node tail = new MockNode();
        Desire desire = new MockDesire();
        Desire desire2 = new MockDesire();
        
        Edge edge1 = new Edge(head, tail, desire);
        Edge edge2 = new Edge(head, tail, desire);
        Edge reversedEdge = new Edge(tail, head, desire);
        Edge diffDesire = new Edge(head, tail, desire2);
        
        Assert.assertEquals(edge1, edge2);
        Assert.assertFalse(edge1.equals(reversedEdge));
        Assert.assertFalse(edge1.equals(diffDesire));
    }
    
    @Test
    public void testHashcode() {
        Node head = new MockNode();
        Node tail = new MockNode();
        Desire desire = new MockDesire();
        
        Edge edge1 = new Edge(head, tail, desire);
        Edge edge2 = new Edge(head, tail, desire);
        
        Set<Edge> edges = new HashSet<Edge>();
        edges.add(edge1);
        Assert.assertTrue(edges.contains(edge2));
    }
}
