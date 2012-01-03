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

import junit.framework.Assert;

import org.grouplens.inject.graph.BindRule;
import org.grouplens.inject.graph.Desire;
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
    
    @Ignore
    @Test
    public void testMultipleSatisfiableDesiresSuccess() throws Exception {
        // Test resolving a single root desire, where the chain of bind rules
        // contains multiple satisfiable desires, and the deepest is selected
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testSimpleContextMatchSuccess() throws Exception {
        // Test that a context-specific bind rule is included and selected
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testContextClosenessMatchSuccess() throws Exception {
        // Test that between two context bind rules, the closest is chosen
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testContextLengthMatchSuccess() throws Exception {
        // Test that between two context bind rules, the longest is chosen
        // if their closeness is equal
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testSingleRootOneDependencySuccess() throws Exception {
        // Test resolving a single root desire, that resolves to a node
        // with a single dependency, which is also then resolved
        Assert.fail();
    }
    
    @Test
    public void testMultipleRootsNoDependenciesSuccess() throws Exception {
        // Test that multiple root desires are resolved, when they have no dependencies
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testMultipleRootsSharedDependencySuccess() throws Exception {
        // Test multiple root desires that resolve to nodes that share
        // a dependency, and verify that the resolved dependency is the same node
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testSingleRootChainedDependenciesSuccess() throws Exception {
        // Test multiple levels of dependencies from a single root desire
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testComplexDependenciesSuccess() throws Exception {
        // Test a contrived example of a reasonably complex dependency scenario
        Assert.fail();
    }
    
    @Test
    public void testLimitedBindRuleApplicationsSuccess() throws Exception {
        // Test that a bind-rule is properly excluded from subsequent desires
        // when resolving a desire chain, but a final desire can still be found
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testLimitedBindRuleApplicationsFail() throws Exception {
        // Test that a bind-rule is properly excluded form subsequent desires
        // but that leaves no applicable bindings so resolving fails
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testCyclicDependenciesFail() throws Exception {
        // Test that a cyclic dependency is properly caught and resolving
        // fails before a stack overflow
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testTooManyBindRulesFail() throws Exception {
        // Test that providing too many choices for bind rules throws an exception
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testUnsatisfiableDesireFail() throws Exception {
        // Test that a chain of desires that cannot be satisfied throws an exception
        Assert.fail();
    }
    
    @Ignore
    @Test
    public void testNonLeafSatisfiableDesireFail() throws Exception {
        // Test that a chain of desires, where an intermediate desire is
        // satisfiable but the leaf node is not, still throws an exception
        Assert.fail();
    }
    
    private static class A {}
    private static class B {}
    private static class C {}
    
    private static class Ap extends A {}
    private static class Bp extends B {}
    private static class Cp extends C {}
}
