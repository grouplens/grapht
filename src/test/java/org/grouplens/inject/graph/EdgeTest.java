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

import org.junit.Test;

public class EdgeTest {
    @Test
    public void testGetters() {
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        Object ep = new Object();
        
        Edge<Object, Object> edge = new Edge<Object, Object>(head, tail, ep);
        
        Assert.assertEquals(head, edge.getHead());
        Assert.assertEquals(tail, edge.getTail());
        Assert.assertEquals(ep, edge.getPayload());
    }
    
    @Test
    public void testEquals() {
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        Object ep1 = new Object();
        Object ep2 = new Object();
        
        Edge<Object, Object> edge1 = new Edge<Object, Object>(head, tail, ep1);
        Edge<Object, Object> edge2 = new Edge<Object, Object>(head, tail, ep1);
        Edge<Object, Object> reversedEdge = new Edge<Object, Object>(tail, head, ep1);
        Edge<Object, Object> diffPayload = new Edge<Object, Object>(head, tail, ep2);
        
        Assert.assertEquals(edge1, edge1);
        // Edges use instance equality
        Assert.assertFalse(edge1.equals(edge2));
        Assert.assertFalse(edge1.equals(reversedEdge));
        Assert.assertFalse(edge1.equals(diffPayload));
    }
    
    @Test
    public void testHashcode() {
        Node<Object> head = new Node<Object>(new Object());
        Node<Object> tail = new Node<Object>(new Object());
        Object ep = new Object();
        
        Edge<Object, Object> edge1 = new Edge<Object, Object>(head, tail, ep);
        Edge<Object, Object> edge2 = new Edge<Object, Object>(head, tail, ep);
        
        Set<Edge<Object, Object>> edges = new HashSet<Edge<Object, Object>>();
        edges.add(edge1);
        
        // Edges use instance equality and hashing, so make sure that edge2
        // doesn't appear in the set
        Assert.assertTrue(edges.contains(edge1));
        Assert.assertFalse(edges.contains(edge2));
    }
}
