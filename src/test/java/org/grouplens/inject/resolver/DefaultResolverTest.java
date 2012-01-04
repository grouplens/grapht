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
package org.grouplens.inject.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.grouplens.inject.graph.BindRule;
import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Edge;
import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.graph.MockBindRule;
import org.grouplens.inject.graph.MockDesire;
import org.grouplens.inject.graph.MockNode;
import org.grouplens.inject.graph.MockNodeRepository;
import org.grouplens.inject.graph.Node;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class DefaultResolverTest {
    private Resolver resolver;

    @Before
    public void setup() {
        resolver = new DefaultResolver(new MockNodeRepository());
    }

    @Test
    public void testSatisfiableDesireNoDependenciesSuccess() throws Exception {
        // Test resolving a single root desire that is already satisfiable
        Node node = new MockNode(A.class, new ArrayList<Desire>());
        Desire desire = new MockDesire(node);

        Graph g = resolver.resolve(Collections.singleton(desire), new HashMap<ContextChain, Collection<? extends BindRule>>());
        Assert.assertEquals(1, g.getNodes().size());
        Assert.assertEquals(node, g.getNodes().iterator().next());
        Assert.assertTrue(g.getOutgoingEdges(node).isEmpty());
    }

    @Test
    public void testSingleDesireOneBindingSuccess() throws Exception {
        // Test resolving a single root desire that has a single bind rule
        Node node = new MockNode(A.class, new ArrayList<Desire>());
        Desire rootDesire = new MockDesire();
        Desire finalDesire = new MockDesire(node);

        MockBindRule rule = new MockBindRule();
        rule.addMapping(rootDesire, finalDesire);
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), Arrays.asList(rule));

        Graph g = resolver.resolve(Collections.singleton(rootDesire), bindings);
        Assert.assertEquals(1, g.getNodes().size());
        Assert.assertEquals(node, g.getNodes().iterator().next());
        Assert.assertTrue(g.getOutgoingEdges(node).isEmpty());
    }

    @Test
    public void testSingleRootChainedDesiresSuccess() throws Exception {
        // Test resolving a single root desire through a chain of bind rules
        Node node = new MockNode(A.class, new ArrayList<Desire>());
        Desire rootDesire = new MockDesire();
        Desire intermediateDesire = new MockDesire();
        Desire finalDesire = new MockDesire(node);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(rootDesire, intermediateDesire), 
                                   new MockBindRule(intermediateDesire, finalDesire)));

        Graph g = resolver.resolve(Collections.singleton(rootDesire), bindings);
        Assert.assertEquals(1, g.getNodes().size());
        Assert.assertEquals(node, g.getNodes().iterator().next());
        Assert.assertTrue(g.getOutgoingEdges(node).isEmpty());
    }

    @Test
    public void testMultipleSatisfiableDesiresSuccess() throws Exception {
        // Test resolving a single root desire, where the chain of bind rules
        // contains multiple satisfiable desires, and the deepest is selected
        Node firstNode = new MockNode(A.class, new ArrayList<Desire>());
        Node secondNode = new MockNode(B.class, new ArrayList<Desire>());

        Desire rootDesire = new MockDesire(firstNode);
        Desire finalDesire = new MockDesire(secondNode);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(rootDesire, finalDesire))); 

        Graph g = resolver.resolve(Collections.singleton(rootDesire), bindings);
        Assert.assertEquals(1, g.getNodes().size());
        Assert.assertEquals(secondNode, g.getNodes().iterator().next());
        Assert.assertTrue(g.getOutgoingEdges(secondNode).isEmpty());
        Assert.assertFalse(g.getNodes().contains(firstNode));
    }

    @Test
    public void testSimpleContextMatchSuccess() throws Exception {
        // Test that a context-specific bind rule is included and selected
        Desire aDep = new MockDesire(); // Desire for A
        Desire bDep = new MockDesire(); // Desire for B

        Node rootNode = new MockNode(Ap.class, Arrays.asList(bDep));
        Node depNode = new MockNode(Bp.class, Arrays.<Desire>asList());

        Desire aBinding = new MockDesire(rootNode); // Ap
        Desire bBinding = new MockDesire(depNode); // Bp

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Ap.class))),
                     Arrays.asList(new MockBindRule(bDep, bBinding))); 

        Graph g = resolver.resolve(Collections.singleton(aDep), bindings);

        Assert.assertEquals(2, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(rootNode));
        Assert.assertTrue(g.getNodes().contains(depNode));

        Assert.assertEquals(g.getOutgoingEdges(rootNode), g.getEdges(rootNode, depNode));
        Assert.assertEquals(g.getOutgoingEdges(rootNode), g.getIncomingEdges(depNode));

        Assert.assertEquals(1, g.getOutgoingEdges(rootNode).size());
        Edge edge = g.getOutgoingEdges(rootNode).iterator().next();
        Assert.assertEquals(rootNode, edge.getHead());
        Assert.assertEquals(depNode, edge.getTail());
        Assert.assertEquals(bDep, edge.getDesire());
    }

    @Test
    public void testContextClosenessMatchSuccess() throws Exception {
        // Test that between two context bind rules, the closest is chosen
        Desire aDep = new MockDesire(); // for A
        Desire bDep = new MockDesire(); // for B
        Desire cDep = new MockDesire(); // for C

        Node n1 = new MockNode(Ap.class, Arrays.asList(bDep));
        Node n2 = new MockNode(Bp.class, Arrays.asList(cDep));
        Node n3 = new MockNode(Cp.class, Arrays.<Desire>asList());
        Node n4 = new MockNode(Cp.class, Arrays.<Desire>asList());

        Desire aBinding = new MockDesire(n1);
        Desire bBinding = new MockDesire(n2);
        Desire cBinding = new MockDesire(n3);
        Desire ocBinding = new MockDesire(n4);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding)));
        // for this test, Ap is a farther away context, so the ocBinding should not
        // be selected and the cBinding will be used
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Ap.class))),
                     Arrays.asList(new MockBindRule(cDep, ocBinding)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Bp.class))),
                     Arrays.asList(new MockBindRule(cDep, cBinding)));
        Graph g = resolver.resolve(Arrays.asList(aDep), bindings);

        Assert.assertEquals(3, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(n1));
        Assert.assertTrue(g.getNodes().contains(n2));
        Assert.assertTrue(g.getNodes().contains(n3));
        Assert.assertFalse(g.getNodes().contains(n4));

        Set<Edge> e1 = g.getOutgoingEdges(n1);
        Set<Edge> e2 = g.getOutgoingEdges(n2);

        Assert.assertEquals(1, e1.size());
        Assert.assertEquals(new Edge(n1, n2, bDep), e1.iterator().next());

        Assert.assertEquals(1, e2.size());
        Assert.assertEquals(new Edge(n2, n3, cDep), e2.iterator().next());
    }

    @Test
    public void testContextLengthMatchSuccess() throws Exception {
        // Test that between two context bind rules, the longest is chosen
        // if their closeness is equal
        Desire aDep = new MockDesire(); // for A
        Desire bDep = new MockDesire(); // for B
        Desire cDep = new MockDesire(); // for C

        Node n1 = new MockNode(Ap.class, Arrays.asList(bDep));
        Node n2 = new MockNode(Bp.class, Arrays.asList(cDep));
        Node n3 = new MockNode(Cp.class, Arrays.<Desire>asList());
        Node n4 = new MockNode(Cp.class, Arrays.<Desire>asList());

        Desire aBinding = new MockDesire(n1);
        Desire bBinding = new MockDesire(n2);
        Desire cBinding = new MockDesire(n3);
        Desire ocBinding = new MockDesire(n4);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding)));
        // for this test, Bp is shorter than Ap,Bp context, so the ocBinding should not
        // be selected and the cBinding will be used
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Bp.class))),
                     Arrays.asList(new MockBindRule(cDep, ocBinding)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Ap.class), 
                                                    new MockContextMatcher(Bp.class))),
                     Arrays.asList(new MockBindRule(cDep, cBinding)));
        Graph g = resolver.resolve(Arrays.asList(aDep), bindings);

        Assert.assertEquals(3, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(n1));
        Assert.assertTrue(g.getNodes().contains(n2));
        Assert.assertTrue(g.getNodes().contains(n3));
        Assert.assertFalse(g.getNodes().contains(n4));

        Set<Edge> e1 = g.getOutgoingEdges(n1);
        Set<Edge> e2 = g.getOutgoingEdges(n2);

        Assert.assertEquals(1, e1.size());
        Assert.assertEquals(new Edge(n1, n2, bDep), e1.iterator().next());

        Assert.assertEquals(1, e2.size());
        Assert.assertEquals(new Edge(n2, n3, cDep), e2.iterator().next());
    }
    
    @Test
    public void testIgnoreDefaultContextBindingSuccess() throws Exception {
        // Test that a specific context binding is preferred over a valid default
        // context binding for the same type. This can be inferred from the above
        // tests but it is nice to ensure it works as expected
        Desire aDep = new MockDesire(); // Desire for A
        Desire bDep = new MockDesire(); // Desire for B

        Node rootNode = new MockNode(Ap.class, Arrays.asList(bDep));
        Node depNode = new MockNode(Bp.class, Arrays.<Desire>asList());
        Node ignoredNode = new MockNode(Bp.class, Arrays.<Desire>asList());
        
        Desire aBinding = new MockDesire(rootNode); // Ap
        Desire bBinding = new MockDesire(depNode); // Bp
        Desire obBinding = new MockDesire(ignoredNode); // other Bp

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, obBinding)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Ap.class))),
                     Arrays.asList(new MockBindRule(bDep, bBinding)));

        Graph g = resolver.resolve(Collections.singleton(aDep), bindings);

        Assert.assertEquals(2, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(rootNode));
        Assert.assertTrue(g.getNodes().contains(depNode));
        Assert.assertFalse(g.getNodes().contains(obBinding));

        Assert.assertEquals(g.getOutgoingEdges(rootNode), g.getEdges(rootNode, depNode));
        Assert.assertEquals(g.getOutgoingEdges(rootNode), g.getIncomingEdges(depNode));

        Assert.assertEquals(1, g.getOutgoingEdges(rootNode).size());
        Edge edge = g.getOutgoingEdges(rootNode).iterator().next();
        Assert.assertEquals(rootNode, edge.getHead());
        Assert.assertEquals(depNode, edge.getTail());
        Assert.assertEquals(bDep, edge.getDesire());
    }

    @Test
    public void testSingleRootOneDependencySuccess() throws Exception {
        // Test resolving a single root desire, that resolves to a node
        // with a single dependency, which is also then resolved
        Desire aDep = new MockDesire(); // Desire for A
        Desire bDep = new MockDesire(); // Desire for B

        Node rootNode = new MockNode(Ap.class, Arrays.asList(bDep));
        Node depNode = new MockNode(Bp.class, Arrays.<Desire>asList());

        Desire aBinding = new MockDesire(rootNode); // Ap
        Desire bBinding = new MockDesire(depNode); // Bp

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding))); 

        Graph g = resolver.resolve(Collections.singleton(aDep), bindings);

        Assert.assertEquals(2, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(rootNode));
        Assert.assertTrue(g.getNodes().contains(depNode));

        Assert.assertEquals(g.getOutgoingEdges(rootNode), g.getEdges(rootNode, depNode));
        Assert.assertEquals(g.getOutgoingEdges(rootNode), g.getIncomingEdges(depNode));

        Assert.assertEquals(1, g.getOutgoingEdges(rootNode).size());
        Edge edge = g.getOutgoingEdges(rootNode).iterator().next();
        Assert.assertEquals(rootNode, edge.getHead());
        Assert.assertEquals(depNode, edge.getTail());
        Assert.assertEquals(bDep, edge.getDesire());
    }

    @Test
    public void testMultipleRootsNoDependenciesSuccess() throws Exception {
        // Test that multiple root desires are resolved, when they have no dependencies
        Node rn1 = new MockNode(A.class, Arrays.<Desire>asList());
        Node rn2 = new MockNode(B.class, Arrays.<Desire>asList());
        Node rn3 = new MockNode(C.class, Arrays.<Desire>asList());

        Desire rd1 = new MockDesire(rn1);
        Desire rd2 = new MockDesire(rn2);
        Desire rd3 = new MockDesire(rn3);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        Graph g = resolver.resolve(Arrays.asList(rd1, rd2, rd3), bindings);

        Assert.assertEquals(3, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(rn1));
        Assert.assertTrue(g.getNodes().contains(rn2));
        Assert.assertTrue(g.getNodes().contains(rn3));

        Assert.assertTrue(g.getOutgoingEdges(rn1).isEmpty());
        Assert.assertTrue(g.getOutgoingEdges(rn2).isEmpty());
        Assert.assertTrue(g.getOutgoingEdges(rn3).isEmpty());
    }

    @Test
    public void testMultipleRootsSharedDependencySuccess() throws Exception {
        // Test multiple root desires that resolve to nodes that share
        // a dependency, and verify that the resolved dependency is the same node
        Desire aDep = new MockDesire(); // for A
        Desire bDep = new MockDesire(); // for B
        Desire cDep = new MockDesire(); // for C

        Node rn1 = new MockNode(Ap.class, Arrays.asList(cDep));
        Node rn2 = new MockNode(Bp.class, Arrays.asList(cDep));
        Node nd = new MockNode(Cp.class, Arrays.<Desire>asList());


        Desire aBinding = new MockDesire(rn1);
        Desire bBinding = new MockDesire(rn2);
        Desire cBinding = new MockDesire(nd);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding),
                                   new MockBindRule(cDep, cBinding)));
        Graph g = resolver.resolve(Arrays.asList(aDep, bDep), bindings);

        Assert.assertEquals(3, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(rn1));
        Assert.assertTrue(g.getNodes().contains(rn2));
        Assert.assertTrue(g.getNodes().contains(nd));

        Set<Edge> e1 = g.getOutgoingEdges(rn1);
        Set<Edge> e2 = g.getOutgoingEdges(rn2);

        Assert.assertEquals(1, e1.size());
        Assert.assertEquals(new Edge(rn1, nd, cDep), e1.iterator().next());

        Assert.assertEquals(1, e2.size());
        Assert.assertEquals(new Edge(rn2, nd, cDep), e2.iterator().next());
    }

    @Test
    public void testSingleRootChainedDependenciesSuccess() throws Exception {
        // Test multiple levels of dependencies from a single root desire
        Desire aDep = new MockDesire(); // for A
        Desire bDep = new MockDesire(); // for B
        Desire cDep = new MockDesire(); // for C

        Node n1 = new MockNode(Ap.class, Arrays.asList(bDep));
        Node n2 = new MockNode(Bp.class, Arrays.asList(cDep));
        Node n3 = new MockNode(Cp.class, Arrays.<Desire>asList());

        Desire aBinding = new MockDesire(n1);
        Desire bBinding = new MockDesire(n2);
        Desire cBinding = new MockDesire(n3);

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding),
                                   new MockBindRule(cDep, cBinding)));
        Graph g = resolver.resolve(Arrays.asList(aDep), bindings);

        Assert.assertEquals(3, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(n1));
        Assert.assertTrue(g.getNodes().contains(n2));
        Assert.assertTrue(g.getNodes().contains(n3));

        Set<Edge> e1 = g.getOutgoingEdges(n1);
        Set<Edge> e2 = g.getOutgoingEdges(n2);

        Assert.assertEquals(1, e1.size());
        Assert.assertEquals(new Edge(n1, n2, bDep), e1.iterator().next());

        Assert.assertEquals(1, e2.size());
        Assert.assertEquals(new Edge(n2, n3, cDep), e2.iterator().next());
    }

    @Ignore
    @Test
    public void testComplexDependenciesSuccess() throws Exception {
        // Test a contrived example of a reasonably complex dependency scenario
        Assert.fail();
    }
    
    @Test
    public void testContextBreakingCycleSuccess() throws Exception {
        // Test that a context that is activated after a certain number of
        // cycles within a dependency can break out of the cycle and finish resolving
        Desire aDep = new MockDesire();
        Desire bDep = new MockDesire();
        
        Node na = new MockNode(Ap.class, Arrays.asList(bDep));
        Node nb1 = new MockNode(B.class, Arrays.asList(aDep));
        Node nb2 = new MockNode(Bp.class, Arrays.<Desire>asList());
        
        Desire aBinding = new MockDesire(na);
        Desire bBinding1 = new MockDesire(nb1);
        Desire bBinding2 = new MockDesire(nb2);
        
        // configure bindings so that na and nb1 cycle for a couple of iterations
        // until the context B/B/B/B is reached, then switch to nb2
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding1)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(B.class),
                                                    new MockContextMatcher(B.class),
                                                    new MockContextMatcher(B.class),
                                                    new MockContextMatcher(B.class))),
                     Arrays.asList(new MockBindRule(bDep, bBinding2)));
        
        Graph g = resolver.resolve(Collections.singleton(aDep), bindings);
        
        // FIXME: This is what the resolver produces currently, which makes sense
        // on some level but it is not particularly useful for reconstructing instances
        Assert.assertEquals(3, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(na));
        Assert.assertTrue(g.getNodes().contains(nb1));
        Assert.assertTrue(g.getNodes().contains(nb2));
        
        Set<Edge> ea = g.getOutgoingEdges(na);
        Assert.assertEquals(2, ea.size());
        Assert.assertTrue(ea.contains(new Edge(na, nb1, bDep)));
        Assert.assertTrue(ea.contains(new Edge(na, nb2, bDep)));
        
        // This test is not valid, we need to rewrite the resolver to handle splitting
        // nodes (as well as the graph api)
        Assert.fail();
    }
    
    @Test
    public void testTooManyChoicesFilteredByContextSuccess() throws Exception {
        // Test when too many choices exist, but are limited in scope by contexts
        // that do not apply, the resolving will succeed
        Desire aDep = new MockDesire();
        
        Node na1 = new MockNode(Ap.class, Arrays.<Desire>asList());
        Node na2 = new MockNode(A.class, Arrays.<Desire>asList());
        
        Desire aBinding1 = new MockDesire(na1);
        Desire aBinding2 = new MockDesire(na2);
        
        // configure bindings so that aDep has two solutions, but 1 is only active
        // within a Bp context (which is not possible in this case)
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding1)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Bp.class))),
                     Arrays.asList(new MockBindRule(aDep, aBinding2)));
        
        Graph g = resolver.resolve(Collections.singleton(aDep), bindings);
        
        Assert.assertEquals(1, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(na1));
        Assert.assertFalse(g.getNodes().contains(na2));
    }

    @Test
    public void testLimitedBindRuleApplicationsSuccess() throws Exception {
        // Test that a bind-rule is properly excluded from subsequent desires
        // when resolving a desire chain, but a final desire can still be found
        Desire aDep = new MockDesire();
        Desire bDep = new MockDesire();
        
        Node na = new MockNode(Ap.class, Arrays.asList(bDep));
        Node nb = new MockNode(Bp.class, Arrays.<Desire>asList());
        
        Desire aBinding = new MockDesire(na);
        Desire obBinding = new MockDesire(nb);
        
        // configure bindings so that A->Ap, A:B->B, B->Bp
        // this means that B->B is first selected to resolve bDep but then
        // it cannot be used further so B->Bp is used, and obBinding is final desire
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding), 
                                   new MockBindRule(bDep, obBinding)));
        bindings.put(new ContextChain(Arrays.asList(new MockContextMatcher(Ap.class))),
                     Arrays.asList(new MockBindRule(bDep, bDep)));
        
        Graph g = resolver.resolve(Collections.singleton(aDep), bindings);
        
        Assert.assertEquals(2, g.getNodes().size());
        Assert.assertTrue(g.getNodes().contains(na));
        Assert.assertTrue(g.getNodes().contains(nb));
    }

    @Test
    public void testLimitedBindRuleApplicationsFail() throws Exception {
        // Test that a bind-rule is properly excluded form subsequent desires
        // but that leaves no applicable bindings so resolving fails
        Desire aDep = new MockDesire();
        Desire bDep = new MockDesire();
        
        Node na = new MockNode(Ap.class, Arrays.asList(bDep));
        
        Desire aBinding = new MockDesire(na);
        
        // configure bindings so that A->Ap, B->B
        // we can no longer use the B->B binding so it fails
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding), 
                                   new MockBindRule(bDep, bDep)));
        
        try {
            resolver.resolve(Collections.singleton(aDep), bindings);
            Assert.fail();
        } catch(ResolverException re) {
            // expected
        }
    }
    
    @Test
    public void testCyclicDependenciesFail() throws Exception {
        // Test that a cyclic dependency is properly caught and resolving
        // fails before a stack overflow
        Desire aDep = new MockDesire(); // for A
        Desire bDep = new MockDesire(); // for B
        
        Node an = new MockNode(Ap.class, Arrays.asList(bDep));
        Node bn = new MockNode(Bp.class, Arrays.asList(aDep));
        
        Desire aBinding = new MockDesire(an);
        Desire bBinding = new MockDesire(bn);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding),
                                   new MockBindRule(bDep, bBinding)));
        
        try {
            resolver.resolve(Arrays.asList(aDep), bindings);
            Assert.fail();
        } catch(ResolverException e) {
            // expected
        }
    }

    @Test
    public void testTooManyBindRulesFail() throws Exception {
        // Test that providing too many choices for bind rules throws an exception
        Desire aDep = new MockDesire(); // Desire for A
        Desire bDep = new MockDesire(); // Desire for B

        Node rootNode = new MockNode(Ap.class, Arrays.asList(bDep));
        Node depNode = new MockNode(Bp.class, Arrays.<Desire>asList());
        Node otherDepNode = new MockNode(Bp.class, Arrays.<Desire>asList());
        
        Desire aBinding = new MockDesire(rootNode); // Ap
        Desire bBinding = new MockDesire(depNode); // Bp
        Desire obBinding = new MockDesire(otherDepNode); // other Bp

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(aDep, aBinding), 
                                   new MockBindRule(bDep, obBinding),
                                   new MockBindRule(bDep, bBinding)));

        try {
            resolver.resolve(Collections.singleton(aDep), bindings);
            Assert.fail();
        } catch(ResolverException e) {
            // expected
        }
    }

    @Test
    public void testUnsatisfiableDesireFail() throws Exception {
        // Test that a chain of desires that cannot be satisfied throws an exception
        MockDesire d1 = new MockDesire();
        MockDesire d2 = new MockDesire();
        MockDesire d3 = new MockDesire();
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(d1, d2), 
                                   new MockBindRule(d2, d3)));
        try {
            resolver.resolve(Collections.singleton(d1), bindings);
            Assert.fail();
        } catch(ResolverException e) {
            // expected
        }
    }
    
    @Test
    public void testNoBindRulesFail() throws Exception {
        // Test that not providing applicable bind rules will throw an exception,
        // even if other bind rules are given
        Node rn = new MockNode();
        Desire rd = new MockDesire();
        
        Desire dep = new MockDesire();
        Desire binding = new MockDesire(rn);
        
        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(dep, binding)));

        try {
            resolver.resolve(Collections.singleton(rd), bindings);
            Assert.fail();
        } catch(ResolverException e) {
            // expected
        }
    }

    @Test
    public void testNonLeafSatisfiableDesireFail() throws Exception {
        // Test that a chain of desires, where an intermediate desire is
        // satisfiable but the leaf node is not, still throws an exception
        Node firstNode = new MockNode(A.class, new ArrayList<Desire>());

        Desire rootDesire = new MockDesire(firstNode);
        Desire finalDesire = new MockDesire();

        Map<ContextChain, Collection<? extends BindRule>> bindings = new HashMap<ContextChain, Collection<? extends BindRule>>();
        bindings.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new MockBindRule(rootDesire, finalDesire))); 

        try {
            resolver.resolve(Collections.singleton(rootDesire), bindings);
            Assert.fail();
        } catch(ResolverException r) {
            // expected
        }
    }

    private static class A {}
    private static class B {}
    private static class C {}

    private static class Ap extends A {}
    private static class Bp extends B {}
    private static class Cp extends C {}
}
