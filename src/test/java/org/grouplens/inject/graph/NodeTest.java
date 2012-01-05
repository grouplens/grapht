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
        Assert.assertEquals(n1.hashCode(), n2.hashCode());
    }
}
