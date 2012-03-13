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
package org.grouplens.inject.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.grouplens.inject.InjectorConfiguration;
import org.grouplens.inject.MockInjectorConfiguration;
import org.grouplens.inject.graph.Edge;
import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.graph.Node;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.MockBindRule;
import org.grouplens.inject.spi.MockContextMatcher;
import org.grouplens.inject.spi.MockDesire;
import org.grouplens.inject.spi.MockRole;
import org.grouplens.inject.spi.MockSatisfaction;
import org.grouplens.inject.spi.Satisfaction;
import org.junit.Test;

public class DefaultResolverTest {
    private Resolver createResolver(Map<ContextChain, Collection<? extends BindRule>> rules) {
        InjectorConfiguration config = new MockInjectorConfiguration(rules);
        return new DefaultResolver(config);
    }
    
    // bypass synthetic root and return node that resolves the desire 
    private Node<Satisfaction> getRoot(Resolver r, Desire d) {
        return r.getGraph().getOutgoingEdge(r.getGraph().getNode(null), d).getTail();
    }
    
    @Test
    public void testNoDependenciesSuccess() throws Exception {
        // Test resolving a satisfaction that has no dependencies and is already satisfiable
        Satisfaction sat = new MockSatisfaction(A.class, new ArrayList<Desire>());
        Desire desire = new MockDesire(sat);

        Resolver r = createResolver(new HashMap<ContextChain, Collection<? extends BindRule>>());
        r.resolve(desire);
        Assert.assertEquals(1 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        
        Node<Satisfaction> node = getRoot(r, desire);
        Assert.assertEquals(sat, node.getLabel());
        Assert.assertTrue(r.getGraph().getOutgoingEdges(node).isEmpty());
        Assert.assertTrue(r.getGraph().getNodes().contains(node));
    }

    @Test
    public void testSingleDependencySuccess() throws Exception {
        // Test resolving a satisfaction with a single dependency that is already satisfiable
        Satisfaction dep = new MockSatisfaction(B.class);
        Desire depDesire = new MockDesire(dep);
        Satisfaction rootSat = new MockSatisfaction(A.class, Arrays.asList(depDesire));
        Desire rootDesire = new MockDesire(rootSat);
        
        Resolver r = createResolver(new HashMap<ContextChain, Collection<? extends BindRule>>());
        r.resolve(rootDesire);
        
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(rootSat, rootNode.getLabel());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(rootNode).size());
        Assert.assertEquals(dep, r.getGraph().getOutgoingEdges(rootNode).iterator().next().getTail().getLabel());
    }

    @Test
    public void testSingleDependencyChainedDesiresSuccess() throws Exception {
        // Test resolving a satisfaction with a single dependency through multiple desires/bind rules
        Satisfaction dep = new MockSatisfaction(B.class);
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Desire d3 = new MockDesire(dep);
        Satisfaction root = new MockSatisfaction(A.class, Arrays.asList(d1));
        Desire rootDesire = new MockDesire(root);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, d2), 
                                   new MockBindRule(d2, d3)));
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(root, rootNode.getLabel());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(rootNode).size());
        Assert.assertEquals(dep, r.getGraph().getOutgoingEdges(rootNode).iterator().next().getTail().getLabel());
    }

    @Test
    public void testMultipleSatisfiableDesiresSuccess() throws Exception {
        // Test resolving a single satisfaction, where the chain of bind rules
        // contains multiple satisfiable desires, and the deepest is selected
        Satisfaction dep = new MockSatisfaction(B.class);
        Satisfaction s1 = new MockSatisfaction(C.class);
        Satisfaction s2 = new MockSatisfaction(Ap.class);
        Desire d1 = new MockDesire(s1);
        Desire d2 = new MockDesire(s2);
        Desire d3 = new MockDesire(dep);
        Satisfaction root = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, d2), 
                                   new MockBindRule(d2, d3)));
        
        Desire rootDesire = new MockDesire(root);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);

        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(root, rootNode.getLabel());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(rootNode).size());
        Assert.assertEquals(dep, r.getGraph().getOutgoingEdges(rootNode).iterator().next().getTail().getLabel());
    }
    
    @Test
    public void testContextRoleMatchSuccess() throws Exception {
        // Test that a roles are properly remembered in the context
        // - note that this is different than having a role-binding, that is
        //   part of the bind rule's match implementation
        MockRole role1 = new MockRole();
        MockRole role2 = new MockRole();
        
        Desire dr1 = new MockDesire(null, role1);
        Desire dr2 = new MockDesire(null, role2);
        Desire d3 = new MockDesire();
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(dr1, dr2));
        Satisfaction r2 = new MockSatisfaction(B.class, Arrays.asList(d3));
        Satisfaction r3 = new MockSatisfaction(C.class, Arrays.asList(d3));
        
        Satisfaction r4 = new MockSatisfaction(Cp.class);
        Satisfaction or4 = new MockSatisfaction(Bp.class);
        
        Desire br1 = new MockDesire(r2);
        Desire br2 = new MockDesire(r3);
        Desire b3 = new MockDesire(r4);
        Desire ob3 = new MockDesire(or4);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(dr1, br1),
                                   new MockBindRule(dr2, br2)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Object.class, role1))),
                     Arrays.asList(new MockBindRule(d3, b3))); 
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Object.class, role2))),
                     Arrays.asList(new MockBindRule(d3, ob3))); 
        
        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(5 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Node<Satisfaction> n1 = getNode(r.getGraph(), r1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), r2);
        Node<Satisfaction> n3 = getNode(r.getGraph(), r3);
        Node<Satisfaction> n4 = getNode(r.getGraph(), r4);
        Node<Satisfaction> on4 = getNode(r.getGraph(), or4);

        Assert.assertEquals(n1, rootNode);
        Assert.assertEquals(2, r.getGraph().getOutgoingEdges(n1).size());
        
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(dr1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n3).size());
        Assert.assertEquals(dr2, r.getGraph().getEdges(n1, n3).iterator().next().getLabel());

        Assert.assertEquals(1, r.getGraph().getEdges(n2, n4).size());
        Assert.assertEquals(d3, r.getGraph().getEdges(n2, n4).iterator().next().getLabel());
        Assert.assertEquals(1, r.getGraph().getEdges(n3, on4).size());
        Assert.assertEquals(d3, r.getGraph().getEdges(n3, on4).iterator().next().getLabel());
    }
    
    @Test
    public void testSimpleContextMatchSuccess() throws Exception {
        // Test that a context-specific bind rule is included and selected
        Desire d1 = new MockDesire();
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction r2 = new MockSatisfaction(B.class);
        Satisfaction or2 = new MockSatisfaction(Bp.class);
        
        Desire b1 = new MockDesire(r2);
        Desire ob1 = new MockDesire(or2);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(A.class))),
                     Arrays.asList(new MockBindRule(d1, ob1))); 

        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);

        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(r1, rootNode.getLabel());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(rootNode).size());
        Assert.assertEquals(or2, r.getGraph().getOutgoingEdges(rootNode).iterator().next().getTail().getLabel());
    }
    
    @Test
    public void testContextClosenessMatchSuccess() throws Exception {
        // Test that between two context bind rules, the closest is chosen
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        
        Satisfaction or3 = new MockSatisfaction(Cp.class);
        Satisfaction r3 = new MockSatisfaction(C.class);
        Satisfaction r2 = new MockSatisfaction(B.class, Arrays.asList(d2));
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Desire b1 = new MockDesire(r2);
        Desire b2 = new MockDesire(r3);
        Desire ob2 = new MockDesire(or3);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1)));
        // for this test, A is farther than B so b2 should be selected over ob2
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(A.class))),
                     Arrays.asList(new MockBindRule(d2, ob2)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(B.class))),
                     Arrays.asList(new MockBindRule(d2, b2)));
        
        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), r1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), r2);
        Node<Satisfaction> n3 = getNode(r.getGraph(), r3);
        Node<Satisfaction> on3 = getNode(r.getGraph(), or3);

        Assert.assertEquals(3 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertNotNull(n3);
        Assert.assertNull(on3);
        
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());

        Assert.assertEquals(1, r.getGraph().getEdges(n2, n3).size());
        Assert.assertEquals(d2, r.getGraph().getEdges(n2, n3).iterator().next().getLabel());
    }

    @Test
    public void testContextLengthMatchSuccess() throws Exception {
        // Test that between two context bind rules, the longest is chosen
        // if their closeness is equal
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        
        Satisfaction or3 = new MockSatisfaction(Cp.class);
        Satisfaction r3 = new MockSatisfaction(C.class);
        Satisfaction r2 = new MockSatisfaction(B.class, Arrays.asList(d2));
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Desire b1 = new MockDesire(r2);
        Desire b2 = new MockDesire(r3);
        Desire ob2 = new MockDesire(or3);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1)));
        // for this test, AB is longer than A so b2 is selected over ob2
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(A.class))),
                     Arrays.asList(new MockBindRule(d2, ob2)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(A.class), new MockContextMatcher(B.class))),
                     Arrays.asList(new MockBindRule(d2, b2)));
        
        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), r1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), r2);
        Node<Satisfaction> n3 = getNode(r.getGraph(), r3);
        Node<Satisfaction> on3 = getNode(r.getGraph(), or3);

        Assert.assertEquals(3 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertNotNull(n3);
        Assert.assertNull(on3);
        
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());

        Assert.assertEquals(1, r.getGraph().getEdges(n2, n3).size());
        Assert.assertEquals(d2, r.getGraph().getEdges(n2, n3).iterator().next().getLabel());
    }
    
    @Test
    public void testIgnoreDefaultContextBindingSuccess() throws Exception {
        // Test that a specific context binding is preferred over a valid default
        // context binding for the same type. This can be inferred from the above
        // tests but it is nice to ensure it works as expected
        Desire d1 = new MockDesire();
        
        Satisfaction or2 = new MockSatisfaction(Bp.class);
        Satisfaction r2 = new MockSatisfaction(B.class);
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Desire b1 = new MockDesire(r2);
        Desire ob1 = new MockDesire(or2);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        // for this test, A is more specific than default, so b2 is selected
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, ob1)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(A.class))),
                     Arrays.asList(new MockBindRule(d1, b1)));
        
        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), r1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), r2);
        Node<Satisfaction> on2 = getNode(r.getGraph(), or2);

        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertNull(on2);
        
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());
    }

    @Test
    public void testMultipleDependenciesSuccess() throws Exception {
        // Test that a satisfaction with multiple dependencies is correctly resolved
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Desire d3 = new MockDesire();
        
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1, d2, d3));
        Satisfaction sd1 = new MockSatisfaction(B.class);
        Satisfaction sd2 = new MockSatisfaction(C.class);
        Satisfaction sd3 = new MockSatisfaction(Cp.class);
        
        Desire b1 = new MockDesire(sd1);
        Desire b2 = new MockDesire(sd2);
        Desire b3 = new MockDesire(sd3);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1),
                                   new MockBindRule(d2, b2),
                                   new MockBindRule(d3, b3)));
        
        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(4 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(r1, rootNode.getLabel());
        Assert.assertEquals(3, r.getGraph().getOutgoingEdges(rootNode).size());
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), sd1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), sd2);
        Node<Satisfaction> n3 = getNode(r.getGraph(), sd3);
        
        Assert.assertEquals(1, r.getGraph().getEdges(rootNode, n1).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(rootNode, n1).iterator().next().getLabel());
        
        Assert.assertEquals(1, r.getGraph().getEdges(rootNode, n2).size());
        Assert.assertEquals(d2, r.getGraph().getEdges(rootNode, n2).iterator().next().getLabel());
        
        Assert.assertEquals(1, r.getGraph().getEdges(rootNode, n3).size());
        Assert.assertEquals(d3, r.getGraph().getEdges(rootNode, n3).iterator().next().getLabel());
    }

    @Test
    public void testMultipleRootsSharedDependencySuccess() throws Exception {
        // Test multiple root desires that resolve to nodes that share
        // a dependency, and verify that the resolved dependency is the same node
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Desire d3 = new MockDesire();
        
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1, d2, d3));
        Satisfaction sd1 = new MockSatisfaction(B.class);
        
        Desire b1 = new MockDesire(sd1);
        Desire b2 = new MockDesire(sd1);
        Desire b3 = new MockDesire(sd1);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1),
                                   new MockBindRule(d2, b2),
                                   new MockBindRule(d3, b3)));
        
        Desire rootDesire = new MockDesire(r1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(r1, rootNode.getLabel());
        Assert.assertEquals(3, r.getGraph().getOutgoingEdges(rootNode).size());
        
        Set<Desire> edges = new HashSet<Desire>();
        for (Edge<Satisfaction, Desire> e: r.getGraph().getOutgoingEdges(rootNode)) {
            Assert.assertEquals(sd1, e.getTail().getLabel());
            edges.add(e.getLabel());
        }
        
        Assert.assertTrue(edges.contains(d1));
        Assert.assertTrue(edges.contains(d2));
        Assert.assertTrue(edges.contains(d3));
    }

    @Test
    public void testChainedDependenciesSuccess() throws Exception {
        // Test multiple levels of dependencies
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        
        Satisfaction s3 = new MockSatisfaction(C.class);
        Satisfaction s2 = new MockSatisfaction(B.class, Arrays.asList(d2));
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire(s3);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1),
                                   new MockBindRule(d2, b2)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(3 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(s1, rootNode.getLabel());
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), s1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), s2);
        Node<Satisfaction> n3 = getNode(r.getGraph(), s3);
        
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());
        
        Assert.assertEquals(1, r.getGraph().getEdges(n2, n3).size());
        Assert.assertEquals(d2, r.getGraph().getEdges(n2, n3).iterator().next().getLabel());
    }

    @Test
    public void testComplexDependenciesSuccess() throws Exception {
        // Test a contrived example of a reasonably complex dependency scenario
        // that tests contexts, roles, shared, and split nodes
        MockRole r1 = new MockRole();
        MockRole r2 = new MockRole();
        MockRole r3 = new MockRole();
        MockRole r4 = new MockRole();
        
        Desire d1 = new MockDesire(null, r1);
        Desire d2 = new MockDesire(null, r2);
        Desire d3 = new MockDesire(null, r3);
        Desire d4 = new MockDesire(null, r4);
        Desire d5 = new MockDesire();
        Desire d6 = new MockDesire();
        Desire d7 = new MockDesire();
        
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1, d2));
        Satisfaction s2 = new MockSatisfaction(B.class, Arrays.asList(d3, d4));
        Satisfaction s3 = new MockSatisfaction(C.class, Arrays.asList(d5));
        Satisfaction s4 = new MockSatisfaction(D.class, Arrays.asList(d6));
        Satisfaction s5 = new MockSatisfaction(E.class, Arrays.asList(d7));
        Satisfaction s6 = new MockSatisfaction(F.class);
        Satisfaction s7 = new MockSatisfaction(G.class);
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire(s3);
        Desire b3 = new MockDesire(s4);
        Desire b4 = new MockDesire(s5);
        Desire b5 = new MockDesire(s6);
        Desire b6 = new MockDesire(s7);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1), // d1 -> s2
                                   new MockBindRule(d2, b1), // d2 -> s2
                                   new MockBindRule(d3, b3), // d3 -> s4
                                   new MockBindRule(d4, b3), // d4 -> s4
                                   new MockBindRule(d5, b4), // d5 -> s5
                                   new MockBindRule(d6, b4), // d6 -> s5
                                   new MockBindRule(d7, b5))); // d7 -> s6
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(B.class, r1))), 
                     Arrays.asList(new MockBindRule(d3, b2))); // r1s1:d3 -> s3
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(B.class, r2), new MockContextMatcher(D.class, r4))),
                     Arrays.asList(new MockBindRule(d7, b6))); // r2s1,r4s2:d7 -> s7
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        // there are 10 nodes, s2, s4 and s5 are duplicated
        Assert.assertEquals(10 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        
        // grab all of the nodes in the graph
        Node<Satisfaction> n1 = rootNode;
        Node<Satisfaction> n2 = getNode(r.getGraph(), n1, s2, d1);
        Node<Satisfaction> on2 = getNode(r.getGraph(), n1, s2, d2);
        Node<Satisfaction> n3 = getNode(r.getGraph(), n2, s3, d3);
        Node<Satisfaction> n4 = getNode(r.getGraph(), n2, s4, d4);
        Node<Satisfaction> on4 = getNode(r.getGraph(), on2, s4, d3); // should equal n4
        Node<Satisfaction> oon4 = getNode(r.getGraph(), on2, s4, d4); // should not equal n4 and on4
        Node<Satisfaction> n5 = getNode(r.getGraph(), n3, s5, d5);
        Node<Satisfaction> on5 = getNode(r.getGraph(), on4, s5, d6); // should equal n5
        Node<Satisfaction> oon5 = getNode(r.getGraph(), oon4, s5, d6); // should not equal n5 and on5
        Node<Satisfaction> n6 = getNode(r.getGraph(), n5, s6, d7);
        Node<Satisfaction> n7 = getNode(r.getGraph(), oon5, s7, d7);
        
        // make sure that node states are as expected, if they're not null then
        // they match the satisfaction and desire in the query
        Assert.assertTrue(n1 != null && n2 != null && n3 != null && n4 != null
                          && n5 != null && n6 != null && n7 != null && on2 != null 
                          && on4 != null && on5 != null && oon4 != null && oon5 != null);
        Assert.assertNotSame(n2, on2);
        Assert.assertSame(n4, on4);
        Assert.assertSame(n5, on5);
        Assert.assertNotSame(n4, oon4);
        Assert.assertNotSame(n5, oon5);
        
        // make sure there aren't any extra edges
        Assert.assertEquals(2, r.getGraph().getOutgoingEdges(n1).size());
        Assert.assertEquals(2, r.getGraph().getOutgoingEdges(n2).size());
        Assert.assertEquals(2, r.getGraph().getOutgoingEdges(on2).size());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(n3).size());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(n4).size());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(oon4).size());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(n5).size());
        Assert.assertEquals(1, r.getGraph().getOutgoingEdges(oon5).size());
        Assert.assertEquals(0, r.getGraph().getOutgoingEdges(n6).size());
        Assert.assertEquals(0, r.getGraph().getOutgoingEdges(n7).size());
        
        // special case for root (since the graph adds a synthetic root)
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(n1).size());
        Assert.assertNull(r.getGraph().getIncomingEdges(n1).iterator().next().getHead().getLabel());
        
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(n2).size());
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(on2).size());
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(n3).size());
        Assert.assertEquals(2, r.getGraph().getIncomingEdges(n4).size());
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(oon4).size());
        Assert.assertEquals(2, r.getGraph().getIncomingEdges(n5).size());
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(oon5).size());
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(n6).size());
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(n7).size());
    }
    
    @Test
    public void testContextBreakingCycleSuccess() throws Exception {
        // Test that a context that is activated after a certain number of
        // cycles within a dependency can break out of the cycle and finish resolving
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class, Arrays.asList(d2));
        Satisfaction os2 = new MockSatisfaction(Bp.class);
        
        Desire b1 = new MockDesire(s2);
        Desire ob1 = new MockDesire(os2);
        Desire b2 = new MockDesire(s1);
        
        // configure bindings so that s1 and s2 cycle for a couple of iterations
        // until the context s2/s2 is reached, then switches to os2
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1),
                                   new MockBindRule(d2, b2)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(B.class),
                                                    new MockContextMatcher(B.class))),
                     Arrays.asList(new MockBindRule(d1, ob1)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        Node<Satisfaction> rootNode = getRoot(r, rootDesire);
        
        // the resulting graph should be s1->s2->s1->s2->s1->os2 = 6 nodes
        Assert.assertEquals(6 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        Assert.assertEquals(s1, rootNode.getLabel());
        
        // edge s1->s2 by d1
        Set<Edge<Satisfaction, Desire>> edges = r.getGraph().getOutgoingEdges(rootNode); 
        Assert.assertEquals(1, edges.size());
        Edge<Satisfaction, Desire> e1 = edges.iterator().next();
        Assert.assertEquals(s2, e1.getTail().getLabel());
        Assert.assertEquals(d1, e1.getLabel());
        
        // edge s2->s1 by d2
        edges = r.getGraph().getOutgoingEdges(e1.getTail()); 
        Assert.assertEquals(1, edges.size());
        Edge<Satisfaction, Desire> e2 = edges.iterator().next();
        Assert.assertEquals(s1, e2.getTail().getLabel());
        Assert.assertEquals(d2, e2.getLabel());
        
        // edge s1->s2 by d1
        edges = r.getGraph().getOutgoingEdges(e2.getTail()); 
        Assert.assertEquals(1, edges.size());
        Edge<Satisfaction, Desire> e3 = edges.iterator().next();
        Assert.assertEquals(s2, e3.getTail().getLabel());
        Assert.assertEquals(d1, e3.getLabel());
        
        // edge s2->s1 by d2
        edges = r.getGraph().getOutgoingEdges(e3.getTail()); 
        Assert.assertEquals(1, edges.size());
        Edge<Satisfaction, Desire> e4 = edges.iterator().next();
        Assert.assertEquals(s1, e4.getTail().getLabel());
        Assert.assertEquals(d2, e4.getLabel());
        
        // edge s1->os2 by d1
        edges = r.getGraph().getOutgoingEdges(e4.getTail()); 
        Assert.assertEquals(1, edges.size());
        Edge<Satisfaction, Desire> e5 = edges.iterator().next();
        Assert.assertEquals(os2, e5.getTail().getLabel());
        Assert.assertEquals(d1, e5.getLabel());
        
        Assert.assertTrue(r.getGraph().getOutgoingEdges(e5.getTail()).isEmpty());
    }
    
    @Test
    public void testTooManyChoicesFilteredByContextSuccess() throws Exception {
        // Test when too many choices exist, but are limited in scope by contexts
        // that do not apply, the resolving will succeed
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class);
        Satisfaction os2 = new MockSatisfaction(Bp.class);
        
        Desire b1 = new MockDesire(s2);
        Desire ob1 = new MockDesire(os2);
        
        // configure bindings so that d1 has two solutions, but 1 is only active
        // within a Bp context (which is not possible in this case)
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Bp.class))),
                     Arrays.asList(new MockBindRule(d1, ob1)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), s1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), s2);
        
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());
    }

    @Test
    public void testLimitedBindRuleApplicationsSuccess() throws Exception {
        // Test that a bind-rule is properly excluded from subsequent desires
        // when resolving a desire chain, but a final desire can still be found
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class);
        
        Desire b1 = new MockDesire(s2);
        
        // configure bindings so that s1:d1->d1, d1->b1
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(A.class))),
                     Arrays.asList(new MockBindRule(d1, d1)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getNodes().size()); // add one for synthetic root
        
        Node<Satisfaction> n1 = getNode(r.getGraph(), s1);
        Node<Satisfaction> n2 = getNode(r.getGraph(), s2);
        
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertEquals(1, r.getGraph().getEdges(n1, n2).size());
        Assert.assertEquals(d1, r.getGraph().getEdges(n1, n2).iterator().next().getLabel());
    }

    @Test(expected=ResolverException.class)
    public void testLimitedBindRuleApplicationsFail() throws Exception {
        // Test that a bind-rule is properly excluded form subsequent desires
        // but that leaves no applicable bindings so resolving fails
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        // configure bindings so that d1->d1 so binding fails
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, d1)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
    }
    
    @Test(expected=ResolverException.class)
    public void testCyclicDependenciesFail() throws Exception {
        // Test that a cyclic dependency is properly caught and resolving
        // fails before a stack overflow
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class, Arrays.asList(d2));
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire(s1);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1),
                                   new MockBindRule(d2, b2)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
    }
    
    @Test(expected=ResolverException.class)
    public void testTooManyBindRulesFail() throws Exception {
        // Test that providing too many choices for bind rules throws an exception
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class);
        Satisfaction s3 = new MockSatisfaction(C.class);
        
        Desire b1 = new MockDesire(s2);
        Desire ob1 = new MockDesire(s3);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1), 
                                   new MockBindRule(d1, ob1)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
    }

    @Test(expected=ResolverException.class)
    public void testUnsatisfiableDesireFail() throws Exception {
        // Test that a chain of desires that cannot be satisfied throws an exception
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Desire d3 = new MockDesire();
        
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, d2), 
                                   new MockBindRule(d2, d3)));
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
    }
    
    @Test(expected=ResolverException.class)
    public void testNoBindRulesFail() throws Exception {
        // Test that not providing applicable bind rules will throw an exception,
        // even if other bind rules are given
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Desire b2 = new MockDesire();
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d2, b2)));

        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
    }

    @Test(expected=ResolverException.class)
    public void testNonLeafSatisfiableDesireFail() throws Exception {
        // Test that a chain of desires, where an intermediate desire is
        // satisfiable but the leaf node is not, still throws an exception
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class);
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire();
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, b1),
                                   new MockBindRule(b1, b2))); 
        
        Desire rootDesire = new MockDesire(s1);
        Resolver r = createResolver(bindings);
        r.resolve(rootDesire);
    }
    
    // Find the node for s connected to p by the given desire, d
    private Node<Satisfaction> getNode(Graph<Satisfaction, Desire> g, Node<Satisfaction> p, Satisfaction s, Desire d) {
        for (Edge<Satisfaction, Desire> e: g.getOutgoingEdges(p)) {
            if (e.getLabel().equals(d) && e.getTail().getLabel().equals(s)) {
                return e.getTail();
            }
        }
        return null;
    }
    
    private Node<Satisfaction> getNode(Graph<Satisfaction, Desire> g, Satisfaction s) {
        return g.getNode(s);
    }

    private static class A {}
    private static class B {}
    private static class C {}
    private static class D {}
    private static class E {}
    private static class F {}
    private static class G {}

    private static class Ap extends A {}
    private static class Bp extends B {}
    private static class Cp extends C {}
}
