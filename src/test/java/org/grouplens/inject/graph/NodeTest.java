/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.graph;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

public class NodeTest {
    @Test
    public void testEquals() {
        // Test that nodes properly conform to instance equality
        Object payload = new Object();
        Node<Object> n1 = new Node<Object>(payload);
        Node<Object> n2 = new Node<Object>(payload);
        
        Assert.assertFalse(n1.equals(n2));
        Assert.assertFalse(n2.equals(n1));
    }
    
    @Test
    public void testHashCode() {
        // Test that nodes properly conform to instance hashcodes
        Object payload = new Object();
        Node<Object> n1 = new Node<Object>(payload);
        Node<Object> n2 = new Node<Object>(payload);
        
        Set<Node<Object>> set = new HashSet<Node<Object>>();
        set.add(n1);
        
        Assert.assertTrue(set.contains(n1));
        Assert.assertFalse(set.contains(n2));
        Assert.assertFalse(n1.hashCode() == n2.hashCode());
    }
}
