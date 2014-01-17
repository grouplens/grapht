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
package org.grouplens.grapht.solver;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.graph.DAGEdge;
import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.context.ContextElements;
import org.grouplens.grapht.context.ContextMatcher;
import org.grouplens.grapht.context.ContextPattern;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class DependencySolverTest {
    private DependencySolver createSolver(ListMultimap<ContextMatcher, BindRule> rules) {
        return DependencySolver.newBuilder()
                .addBindingFunction(new RuleBasedBindingFunction(rules))
                .setDefaultPolicy(CachePolicy.NO_PREFERENCE)
                .setMaxDepth(100)
                .build();
    }
    
    // bypass synthetic root and return node that resolves the desire 
    private DAGNode<CachedSatisfaction, DesireChain> getRoot(DependencySolver r, Desire d) {
        return r.getGraph().getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d)).getTail();
    }
    
    @Test
    public void testCachePolicySuccess() throws Exception {
        // Test that satisfactions formed with different cache policies 
        // correctly restrict merging of nodes
        Satisfaction sa = new MockSatisfaction(A.class, new ArrayList<Desire>());
        Desire da = new MockDesire();
        Satisfaction sb = new MockSatisfaction(B.class, Arrays.asList(da));
        
        Desire ra = new MockDesire(sa);
        Desire rb = new MockDesire(sb);
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        final Class<?> type = B.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(da, ra).setCachePolicy(CachePolicy.MEMOIZE));
        bindings.put(ContextPattern.any(),
                     new MockBindRule(da, ra).setCachePolicy(CachePolicy.NEW_INSTANCE));
        
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rb);
        r.resolve(da); // should create a single extra sa node with different cache policy

        assertThat(r.getGraph().getReachableNodes(),
                   hasSize(3 + 1));

        DAGNode<CachedSatisfaction, DesireChain> bnode = getRoot(r, rb);
        Assert.assertEquals(CachePolicy.NO_PREFERENCE, bnode.getLabel().getCachePolicy());
        
        DAGNode<CachedSatisfaction, DesireChain> adepnode = getNode(bnode, sa, da);
        Assert.assertNotNull(adepnode);
        Assert.assertEquals(CachePolicy.MEMOIZE, adepnode.getLabel().getCachePolicy());
        
        DAGNode<CachedSatisfaction, DesireChain> anode = getRoot(r, da);
        Assert.assertNotSame(adepnode, anode);
        Assert.assertEquals(CachePolicy.NEW_INSTANCE, anode.getLabel().getCachePolicy());
    }
    
    @Test
    public void testNoDependenciesSuccess() throws Exception {
        // Test resolving a satisfaction that has no dependencies and is already satisfiable
        Satisfaction sat = new MockSatisfaction(A.class, new ArrayList<Desire>());
        Desire desire = new MockDesire(sat);

        DependencySolver r = createSolver(ArrayListMultimap.<ContextMatcher, BindRule>create());
        r.resolve(desire);
        assertThat(r.getGraph().getReachableNodes(), hasSize(2));

        DAGNode<CachedSatisfaction, DesireChain> node = getRoot(r, desire);
        Assert.assertEquals(sat, node.getLabel().getSatisfaction());
        Assert.assertTrue(node.getOutgoingEdges().isEmpty());
        Assert.assertTrue(r.getGraph().getReachableNodes().contains(node));
    }

    @Test
    public void testSingleDependencySuccess() throws Exception {
        // Test resolving a satisfaction with a single dependency that is already satisfiable
        Satisfaction dep = new MockSatisfaction(B.class);
        Desire depDesire = new MockDesire(dep);
        Satisfaction rootSat = new MockSatisfaction(A.class, Arrays.asList(depDesire));
        Desire rootDesire = new MockDesire(rootSat);
        
        DependencySolver r = createSolver(ArrayListMultimap.<ContextMatcher, BindRule>create());
        r.resolve(rootDesire);
        
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(rootSat, rootNode.getLabel().getSatisfaction());
        Assert.assertEquals(1, rootNode.getOutgoingEdges().size());
        Assert.assertEquals(dep, rootNode.getOutgoingEdges().iterator().next().getTail().getLabel().getSatisfaction());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, d2),
                        new MockBindRule(d2, d3));
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(root, rootNode.getLabel().getSatisfaction());
        Assert.assertEquals(1, rootNode.getOutgoingEdges().size());
        Assert.assertEquals(dep, rootNode.getOutgoingEdges().iterator().next().getTail().getLabel().getSatisfaction());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, d2),
                        new MockBindRule(d2, d3));

        Desire rootDesire = new MockDesire(root);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);

        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(root, rootNode.getLabel().getSatisfaction());
        Assert.assertEquals(1, rootNode.getOutgoingEdges().size());
        Assert.assertEquals(dep, rootNode.getOutgoingEdges().iterator().next().getTail().getLabel().getSatisfaction());
    }
    
    @Test
    public void testParentReferencesGrandChildSuccess() throws Exception {
        // Test that when a desire references a child that child's desire (2 desires total),
        // the parent desire is resolved properly (note that a solution by walking
        // up leaf nodes doesn't work in this case since it will encounter the parent
        // and child at the same time).
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        
        Satisfaction s3 = new MockSatisfaction(C.class); // leaf/grandchild
        Satisfaction s2 = new MockSatisfaction(B.class, Arrays.asList(d2)); // child
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1, d2));
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire(s3);
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d2, b2));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(3 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(s1, rootNode.getLabel().getSatisfaction());
        
        DAGNode<CachedSatisfaction, DesireChain> n1 =
                getNode(r.getGraph(), new CachedSatisfaction(s1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(s2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(r.getGraph(), new CachedSatisfaction(s3, CachePolicy.NO_PREFERENCE));

        assertThat(Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))),
                   hasSize(1));
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
        
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(d2, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());
        
        Assert.assertEquals(1, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(d2, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());
    }
    
    @Test
    public void testContextRoleMatchSuccess() throws Exception {
        // Test that qualifiers are properly remembered in the context
        // - note that this is different than having a qualifier-binding, that is
        //   part of the bind rule's match implementation
        Qual qualifier1 = AnnotationBuilder.of(Qual.class).setValue(0).build();
        Qual qualifier2 = AnnotationBuilder.of(Qual.class).setValue(1).build();
        
        Desire dr1 = new MockDesire(null, qualifier1);
        Desire dr2 = new MockDesire(null, qualifier2);
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(dr1, br1),
                        new MockBindRule(dr2, br2));
        final Class<?> type = Object.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.match(qualifier1);
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d3, b3));
        final Class<?> type1 = Object.class;
        final MockQualifierMatcher qualifier3 = MockQualifierMatcher.match(qualifier2);
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type1, qualifier3)),
                     new MockBindRule(d3, ob3));
        
        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(5 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(r1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(r2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(r.getGraph(), new CachedSatisfaction(r3, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n4 = getNode(r.getGraph(), new CachedSatisfaction(r4, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> on4 = getNode(r.getGraph(), new CachedSatisfaction(or4, CachePolicy.NO_PREFERENCE));

        Assert.assertEquals(n1, rootNode);
        Assert.assertEquals(2, n1.getOutgoingEdges().size());
        
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(dr1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(dr2, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());

        Assert.assertEquals(1, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n4))).size());
        Assert.assertEquals(d3, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n4))).iterator().next().getLabel().getInitialDesire());
        Assert.assertEquals(1, Sets.filter(n3.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(on4))).size());
        Assert.assertEquals(d3, Sets.filter(n3.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(on4))).iterator().next().getLabel().getInitialDesire());
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

        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, b1));
        final Class<?> type = A.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d1, ob1));

        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);

        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(r1, rootNode.getLabel().getSatisfaction());
        Assert.assertEquals(1, rootNode.getOutgoingEdges().size());
        Assert.assertEquals(or2, rootNode.getOutgoingEdges().iterator().next().getTail().getLabel().getSatisfaction());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, b1));
        // for this test, CycleA is farther than B so b2 should be selected over ob2
        final Class<?> type = A.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d2, ob2));
        final Class<?> type1 = B.class;
        final MockQualifierMatcher qualifier1 = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type1, qualifier1)),
                     new MockBindRule(d2, b2));
        
        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(r1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(r2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(r.getGraph(), new CachedSatisfaction(r3, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> on3 = getNode(r.getGraph(), new CachedSatisfaction(or3, CachePolicy.NO_PREFERENCE));

        Assert.assertEquals(3 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertNotNull(n3);
        Assert.assertNull(on3);
        
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());

        Assert.assertEquals(1, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(d2, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, b1));
        // for this test, AB is longer than CycleA so b2 is selected over ob2
        final Class<?> type = A.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d2, ob2));
        final Class<?> type1 = A.class;
        final MockQualifierMatcher qualifier1 = MockQualifierMatcher.any();
        final Class<?> type2 = B.class;
        final MockQualifierMatcher qualifier2 = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type1, qualifier1), ContextElements.matchType(type2, qualifier2)
        ),
                     new MockBindRule(d2, b2));
        
        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(r1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(r2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(r.getGraph(), new CachedSatisfaction(r3, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> on3 = getNode(r.getGraph(), new CachedSatisfaction(or3, CachePolicy.NO_PREFERENCE));

        Assert.assertEquals(3 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertNotNull(n3);
        Assert.assertNull(on3);
        
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());

        Assert.assertEquals(1, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(d2, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        // for this test, CycleA is more specific than default, so b2 is selected
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, ob1));
        final Class<?> type = A.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d1, b1));
        
        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(r1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(r2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> on2 = getNode(r.getGraph(), new CachedSatisfaction(or2, CachePolicy.NO_PREFERENCE));

        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertNull(on2);
        
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d2, b2),
                        new MockBindRule(d3, b3));
        
        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(4 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(r1, rootNode.getLabel().getSatisfaction());
        Assert.assertEquals(3, rootNode.getOutgoingEdges().size());
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(sd1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(sd2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(r.getGraph(), new CachedSatisfaction(sd3, CachePolicy.NO_PREFERENCE));
        
        Assert.assertEquals(1, Sets.filter(rootNode.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n1))).size());
        Assert.assertEquals(d1, Sets.filter(rootNode.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n1))).iterator().next().getLabel().getInitialDesire());
        
        Assert.assertEquals(1, Sets.filter(rootNode.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d2, Sets.filter(rootNode.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
        
        Assert.assertEquals(1, Sets.filter(rootNode.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(d3, Sets.filter(rootNode.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());
    }

    @Test
    public void testMultipleRootsSharedDependencySuccess() throws Exception {
        // Test multiple root desires that resolve to nodes that share
        // a dependency, and verify that the resolved dependency is the same DAGNode<CachedSatisfaction, DesireChain>
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Desire d3 = new MockDesire();
        
        Satisfaction r1 = new MockSatisfaction(A.class, Arrays.asList(d1, d2, d3));
        Satisfaction sd1 = new MockSatisfaction(B.class);
        
        Desire b1 = new MockDesire(sd1);
        Desire b2 = new MockDesire(sd1);
        Desire b3 = new MockDesire(sd1);

        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d2, b2),
                        new MockBindRule(d3, b3));
        
        Desire rootDesire = new MockDesire(r1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(r1, rootNode.getLabel().getSatisfaction());
        Assert.assertEquals(3, rootNode.getOutgoingEdges().size());
        
        Set<Desire> edges = new HashSet<Desire>();
        for (DAGEdge<CachedSatisfaction, DesireChain> e: rootNode.getOutgoingEdges()) {
            Assert.assertEquals(sd1, e.getTail().getLabel().getSatisfaction());
            edges.add(e.getLabel().getInitialDesire());
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d2, b2));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        Assert.assertEquals(3 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(s1, rootNode.getLabel().getSatisfaction());
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(s1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(s2, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(r.getGraph(), new CachedSatisfaction(s3, CachePolicy.NO_PREFERENCE));
        
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
        
        Assert.assertEquals(1, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).size());
        Assert.assertEquals(d2, Sets.filter(n2.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n3))).iterator().next().getLabel().getInitialDesire());
    }

    @Test
    public void testComplexDependenciesSuccess() throws Exception {
        // Test a contrived example of a reasonably complex dependency scenario
        // that tests contexts, qualifiers, shared, and split nodes
        Qual r1 = AnnotationBuilder.of(Qual.class).setValue(0).build();
        Qual r2 = AnnotationBuilder.of(Qual.class).setValue(1).build();
        Qual r3 = AnnotationBuilder.of(Qual.class).setValue(2).build();
        Qual r4 = AnnotationBuilder.of(Qual.class).setValue(3).build();
        
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
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1), // d1 -> s2
                        new MockBindRule(d2, b1), // d2 -> s2
                        new MockBindRule(d3, b3), // d3 -> s4
                        new MockBindRule(d4, b3), // d4 -> s4
                        new MockBindRule(d5, b4), // d5 -> s5
                        new MockBindRule(d6, b4), // d6 -> s5
                        new MockBindRule(d7, b5)); // d7 -> s6
        final Class<?> type = B.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.match(r1);
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d3, b2)); // r1s1:d3 -> s3
        final Class<?> type1 = B.class;
        final MockQualifierMatcher qualifier1 = MockQualifierMatcher.match(r2);
        final Class<?> type2 = D.class;
        final MockQualifierMatcher qualifier2 = MockQualifierMatcher.match(r4);
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type1, qualifier1),
                                                ContextElements.matchType(type2, qualifier2)
        ),
                     new MockBindRule(d7, b6)); // r2s1,r4s2:d7 -> s7
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        // there are 10 nodes, s2, s4 and s5 are duplicated
        Assert.assertEquals(10 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        
        // grab all of the nodes in the graph
        DAGNode<CachedSatisfaction, DesireChain> n1 = rootNode;
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(n1, s2, d1);
        DAGNode<CachedSatisfaction, DesireChain> on2 = getNode(n1, s2, d2);
        DAGNode<CachedSatisfaction, DesireChain> n3 = getNode(n2, s3, d3);
        DAGNode<CachedSatisfaction, DesireChain> n4 = getNode(n2, s4, d4);
        DAGNode<CachedSatisfaction, DesireChain> on4 = getNode(on2, s4, d3); // should equal n4
        DAGNode<CachedSatisfaction, DesireChain> oon4 = getNode(on2, s4, d4); // should not equal n4 and on4
        DAGNode<CachedSatisfaction, DesireChain> n5 = getNode(n3, s5, d5);
        DAGNode<CachedSatisfaction, DesireChain> on5 = getNode(on4, s5, d6); // should equal n5
        DAGNode<CachedSatisfaction, DesireChain> oon5 = getNode(oon4, s5, d6); // should not equal n5 and on5
        DAGNode<CachedSatisfaction, DesireChain> n6 = getNode(n5, s6, d7);
        DAGNode<CachedSatisfaction, DesireChain> n7 = getNode(oon5, s7, d7);
        
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
        Assert.assertEquals(2, n1.getOutgoingEdges().size());
        Assert.assertEquals(2, n2.getOutgoingEdges().size());
        Assert.assertEquals(2, on2.getOutgoingEdges().size());
        Assert.assertEquals(1, n3.getOutgoingEdges().size());
        Assert.assertEquals(1, n4.getOutgoingEdges().size());
        Assert.assertEquals(1, oon4.getOutgoingEdges().size());
        Assert.assertEquals(1, n5.getOutgoingEdges().size());
        Assert.assertEquals(1, oon5.getOutgoingEdges().size());
        Assert.assertEquals(0, n6.getOutgoingEdges().size());
        Assert.assertEquals(0, n7.getOutgoingEdges().size());
        
        // special case for root (since the graph adds a synthetic root)
        Assert.assertEquals(1, r.getGraph().getIncomingEdges(n1).size());
        assertThat(r.getGraph().getIncomingEdges(n1).iterator().next().getHead().getLabel(),
                   equalTo(DependencySolver.ROOT_SATISFACTION));

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
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d2, b2));
        final Class<?> type = B.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        final Class<?> type1 = B.class;
        final MockQualifierMatcher qualifier1 = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier),
                                                ContextElements.matchType(type1, qualifier1)
        ),
                     new MockBindRule(d1, ob1));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        DAGNode<CachedSatisfaction, DesireChain> rootNode = getRoot(r, rootDesire);
        
        // the resulting graph should be s1->s2->s1->s2->s1->os2 = 6 nodes
        Assert.assertEquals(6 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(s1, rootNode.getLabel().getSatisfaction());
        
        // edge s1->s2 by d1
        Set<DAGEdge<CachedSatisfaction, DesireChain>> edges = rootNode.getOutgoingEdges();
        Assert.assertEquals(1, edges.size());
        DAGEdge<CachedSatisfaction, DesireChain> e1 = edges.iterator().next();
        Assert.assertEquals(s2, e1.getTail().getLabel().getSatisfaction());
        Assert.assertEquals(d1, e1.getLabel().getInitialDesire());
        
        // edge s2->s1 by d2
        edges = e1.getTail().getOutgoingEdges();
        Assert.assertEquals(1, edges.size());
        DAGEdge<CachedSatisfaction, DesireChain> e2 = edges.iterator().next();
        Assert.assertEquals(s1, e2.getTail().getLabel().getSatisfaction());
        Assert.assertEquals(d2, e2.getLabel().getInitialDesire());
        
        // edge s1->s2 by d1
        edges = e2.getTail().getOutgoingEdges();
        Assert.assertEquals(1, edges.size());
        DAGEdge<CachedSatisfaction, DesireChain> e3 = edges.iterator().next();
        Assert.assertEquals(s2, e3.getTail().getLabel().getSatisfaction());
        Assert.assertEquals(d1, e3.getLabel().getInitialDesire());
        
        // edge s2->s1 by d2
        edges = e3.getTail().getOutgoingEdges();
        Assert.assertEquals(1, edges.size());
        DAGEdge<CachedSatisfaction, DesireChain> e4 = edges.iterator().next();
        Assert.assertEquals(s1, e4.getTail().getLabel().getSatisfaction());
        Assert.assertEquals(d2, e4.getLabel().getInitialDesire());
        
        // edge s1->os2 by d1
        edges = e4.getTail().getOutgoingEdges();
        Assert.assertEquals(1, edges.size());
        DAGEdge<CachedSatisfaction, DesireChain> e5 = edges.iterator().next();
        Assert.assertEquals(os2, e5.getTail().getLabel().getSatisfaction());
        Assert.assertEquals(d1, e5.getLabel().getInitialDesire());
        
        Assert.assertTrue(e5.getTail().getOutgoingEdges().isEmpty());
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
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, b1));
        final Class<?> type = Bp.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d1, ob1));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(s1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(s2, CachePolicy.NO_PREFERENCE));
        
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
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
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, b1));
        final Class<?> type = A.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.put(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                     new MockBindRule(d1, d1));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
        
        Assert.assertEquals(2 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        
        DAGNode<CachedSatisfaction, DesireChain> n1 = getNode(r.getGraph(), new CachedSatisfaction(s1, CachePolicy.NO_PREFERENCE));
        DAGNode<CachedSatisfaction, DesireChain> n2 = getNode(r.getGraph(), new CachedSatisfaction(s2, CachePolicy.NO_PREFERENCE));
        
        Assert.assertNotNull(n1);
        Assert.assertNotNull(n2);
        Assert.assertEquals(1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).size());
        Assert.assertEquals(d1, Sets.filter(n1.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(n2))).iterator().next().getLabel().getInitialDesire());
    }
    
    @Test
    public void testMultipleRequestsMergeSuccess() throws Exception {
        // Test that multiple requests to resolve() will update the graph
        // and share dependency nodes as expected
        Desire a1 = new MockDesire(); // a's dependency
        Desire d1 = new MockDesire(); // d's first dependency
        Desire d2 = new MockDesire(); // d's second dependency
        Satisfaction sa = new MockSatisfaction(A.class, Arrays.asList(a1));
        Satisfaction sap = new MockSatisfaction(Ap.class, Arrays.asList(a1)); // variant of CycleA
        Satisfaction sd = new MockSatisfaction(D.class, Arrays.asList(d1, d2));
        Satisfaction sb = new MockSatisfaction(B.class);
        Satisfaction sc = new MockSatisfaction(C.class);
        
        Desire da = new MockDesire(sa);
        Desire dap = new MockDesire(sap);
        
        // configure bindings so that a1 -> sd, b1 -> sb, b2 -> sc
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(a1, new MockDesire(sd)),
                        new MockBindRule(d1, new MockDesire(sb)),
                        new MockBindRule(d2, new MockDesire(sc)));
        
        DependencySolver r = createSolver(bindings.build());
        r.resolve(da);
        r.resolve(dap);
        
        DAGNode<CachedSatisfaction, DesireChain> root = r.getGraph();
        Assert.assertEquals(5 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(2, root.getOutgoingEdges().size()); // da and dap
        
        DAGNode<CachedSatisfaction, DesireChain> na =
                root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(da)).getTail();
        DAGNode<CachedSatisfaction, DesireChain> nap = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(dap)).getTail();
        
        // sa and sap were different satisfactions, so they should be separate nodes
        Assert.assertNotSame(na, nap);
        
        // the resolved desire for a1, from da
        DAGNode<CachedSatisfaction, DesireChain> ra1 = na.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(a1)).getTail();
        DAGNode<CachedSatisfaction, DesireChain> ra1p = nap.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(a1)).getTail();
        
        // verify that both a and ap point to the sb satisfaction, and verify
        // that sb (and also its children) are properly shared
        Assert.assertSame(sd, ra1.getLabel().getSatisfaction());
        Assert.assertSame(sd, ra1p.getLabel().getSatisfaction());
        Assert.assertSame(ra1, ra1p);

        DAGNode<CachedSatisfaction, DesireChain> node =
                Iterables.find(r.getGraph().getReachableNodes(),
                               DAGNode.labelMatches(Predicates.equalTo(new CachedSatisfaction(sd, CachePolicy.NO_PREFERENCE))));
        assertThat(r.getGraph().getIncomingEdges(node),
                   hasSize(2));
    }
    
    @Test
    public void testMultipleRequestsNoMergeSuccess() throws Exception {
        // Test that multiple requests will keep nodes separate as required
        // by dependency configuration
        Desire a1 = new MockDesire(); // a's dependency
        Desire d1 = new MockDesire(); // d's first dependency
        Desire d2 = new MockDesire(); // d's second dependency
        Satisfaction sa = new MockSatisfaction(A.class, Arrays.asList(a1));
        Satisfaction sap = new MockSatisfaction(Ap.class, Arrays.asList(a1)); // variant of CycleA
        Satisfaction sd = new MockSatisfaction(D.class, Arrays.asList(d1, d2));
        Satisfaction sb = new MockSatisfaction(B.class);
        Satisfaction sbp = new MockSatisfaction(Bp.class); // variant of B
        Satisfaction sc = new MockSatisfaction(C.class);
        Satisfaction scp = new MockSatisfaction(Cp.class); // variant of C
        
        Desire da = new MockDesire(sa);
        Desire dap = new MockDesire(sap);
        
        // configure bindings so that a1 -> sd, b1 -> sb, b2 -> sc
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(a1, new MockDesire(sd)),
                        new MockBindRule(d1, new MockDesire(sb)),
                        new MockBindRule(d2, new MockDesire(sc)));
        final Class<?> type = Ap.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.putAll(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                        new MockBindRule(d1, new MockDesire(sbp)),
                        new MockBindRule(d2, new MockDesire(scp)));
        
        DependencySolver r = createSolver(bindings.build());
        r.resolve(da);
        r.resolve(dap);
        
        DAGNode<CachedSatisfaction, DesireChain> root = r.getGraph();
        Assert.assertEquals(8 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(2, root.getOutgoingEdges().size()); // da and dap
        
        DAGNode<CachedSatisfaction, DesireChain> na = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(da)).getTail();
        DAGNode<CachedSatisfaction, DesireChain> nap = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(dap)).getTail();
        
        // sa and sap were different satisfactions, so they should be separate nodes
        Assert.assertNotSame(na, nap);
        
        // the resolved desire for a1, from da
        DAGNode<CachedSatisfaction, DesireChain> ra1 = na.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(a1)).getTail();
        DAGNode<CachedSatisfaction, DesireChain> ra1p = nap.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(a1)).getTail();
        
        // verify that both ra1 and ra1p are different nodes that both use the
        // sd satisfaction because sd's dependencies are configured differently
        Assert.assertNotSame(ra1, ra1p);
        Assert.assertSame(sd, ra1.getLabel().getSatisfaction());
        Assert.assertSame(sd, ra1p.getLabel().getSatisfaction());
        
        Assert.assertSame(sb, ra1.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d1)).getTail().getLabel().getSatisfaction());
        Assert.assertSame(sc, ra1.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d2)).getTail().getLabel().getSatisfaction());
        Assert.assertSame(sbp, ra1p.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d1)).getTail().getLabel().getSatisfaction());
        Assert.assertSame(scp, ra1p.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d2)).getTail().getLabel().getSatisfaction());
    }
    
    @Test
    public void testRequestDependencyMergeSuccess() throws Exception {
        // Test that a request for a desire already in the graph as a dependency
        // will have a new edge from the root to that dependency added.
        Desire a1 = new MockDesire(); // a's dependency
        Desire d1 = new MockDesire(); // d's first dependency
        Desire d2 = new MockDesire(); // d's second dependency
        Satisfaction sa = new MockSatisfaction(A.class, Arrays.asList(a1));
        Satisfaction sd = new MockSatisfaction(D.class, Arrays.asList(d1, d2));
        Satisfaction sb = new MockSatisfaction(B.class);
        Satisfaction sc = new MockSatisfaction(C.class);
        
        Desire da = new MockDesire(sa);
        Desire dd = new MockDesire(sd);
        // configure bindings so that a1 -> sd, b1 -> sb, b2 -> sc
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(a1, new MockDesire(sd)),
                        new MockBindRule(d1, new MockDesire(sb)),
                        new MockBindRule(d2, new MockDesire(sc)));
        
        DependencySolver r = createSolver(bindings.build());
        r.resolve(da);
        r.resolve(dd);
        
        DAGNode<CachedSatisfaction, DesireChain> root = r.getGraph();
        Assert.assertEquals(4 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(2, root.getOutgoingEdges().size()); // da and dd
        
        DAGNode<CachedSatisfaction, DesireChain> na = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(da)).getTail();
        DAGNode<CachedSatisfaction, DesireChain> nd = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(dd)).getTail();
        
        // additionally verify that there is an edge going from na to nd
        Assert.assertEquals(1, Sets.filter(na.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(nd))).size());
        Assert.assertSame(nd, na.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(a1)).getTail());
    }
    
    @Test
    public void testRequestDependencyNoMergeSuccess() throws Exception {
        // Test that a request for a desire already in the graph as a dependency,
        // will create a new node if the dependency has a different configuration
        // because of context-specific bind rules
        Desire a1 = new MockDesire(); // a's dependency
        Desire d1 = new MockDesire(); // d's first dependency
        Desire d2 = new MockDesire(); // d's second dependency
        Satisfaction sa = new MockSatisfaction(A.class, Arrays.asList(a1));
        Satisfaction sd = new MockSatisfaction(D.class, Arrays.asList(d1, d2));
        Satisfaction sb = new MockSatisfaction(B.class);
        Satisfaction sbp = new MockSatisfaction(Bp.class); // variant of B
        Satisfaction sc = new MockSatisfaction(C.class);
        Satisfaction scp = new MockSatisfaction(Cp.class); // variant of C
        
        Desire da = new MockDesire(sa);
        Desire dd = new MockDesire(sd);
        
        // configure bindings so that a1 -> sd, b1 -> sb, b2 -> sc
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(a1, new MockDesire(sd)),
                        new MockBindRule(d1, new MockDesire(sbp)),
                        new MockBindRule(d2, new MockDesire(scp)));
        final Class<?> type = A.class;
        final MockQualifierMatcher qualifier = MockQualifierMatcher.any();
        bindings.putAll(ContextPattern.subsequence(ContextElements.matchType(type, qualifier)),
                        new MockBindRule(d1, new MockDesire(sb)),
                        new MockBindRule(d2, new MockDesire(sc)));
        
        DependencySolver r = createSolver(bindings.build());
        r.resolve(da);
        r.resolve(dd);
        
        DAGNode<CachedSatisfaction, DesireChain> root = r.getGraph();
        Assert.assertEquals(7 + 1, r.getGraph().getReachableNodes().size()); // add one for synthetic root
        Assert.assertEquals(2, root.getOutgoingEdges().size()); // da and dd
        
        // resolved root desire nodes
        DAGNode<CachedSatisfaction, DesireChain> na = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(da)).getTail();
        DAGNode<CachedSatisfaction, DesireChain> nd = root.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(dd)).getTail();
        
        // make sure that there is no edge between na and nd
        Assert.assertTrue(Sets.filter(na.getOutgoingEdges(), DAGEdge.tailMatches(Predicates.equalTo(nd))).isEmpty());
        
        // look up dependency for na (which is also the sd satisfaction)
        DAGNode<CachedSatisfaction, DesireChain> nad = na.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(a1)).getTail();
        
        // verify that the two sd nodes are different and have different edge
        // configurations
        Assert.assertNotSame(nd, nad); 
        Assert.assertSame(sd, nd.getLabel().getSatisfaction());
        Assert.assertSame(sd, nad.getLabel().getSatisfaction());
        
        Assert.assertSame(sb, nad.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d1)).getTail().getLabel().getSatisfaction());
        Assert.assertSame(sc, nad.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d2)).getTail().getLabel().getSatisfaction());
        Assert.assertSame(sbp, nd.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d1)).getTail().getLabel().getSatisfaction());
        Assert.assertSame(scp, nd.getOutgoingEdgeWithLabel(DesireChain.hasInitialDesire(d2)).getTail().getLabel().getSatisfaction());
    }

    @Test(expected=UnresolvableDependencyException.class)
    public void testLimitedBindRuleApplicationsFail() throws Exception {
        // Test that a bind-rule is properly excluded form subsequent desires
        // but that leaves no applicable bindings so resolving fails
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        // configure bindings so that d1->d1 so binding fails
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d1, d1));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
    }
    
    @Test(expected=CyclicDependencyException.class)
    public void testCyclicDependenciesFail() throws Exception {
        // Test that a cyclic dependency is properly caught and resolving
        // fails before a stack overflow
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class, Arrays.asList(d2));
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire(s1);
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d2, b2));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
    }
    
    @Test(expected=MultipleBindingsException.class)
    public void testTooManyBindRulesFail() throws Exception {
        // Test that providing too many choices for bind rules throws an exception
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class);
        Satisfaction s3 = new MockSatisfaction(C.class);
        
        Desire b1 = new MockDesire(s2);
        Desire ob1 = new MockDesire(s3);
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(d1, ob1));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
    }

    @Test(expected=UnresolvableDependencyException.class)
    public void testUnsatisfiableDesireFail() throws Exception {
        // Test that a chain of desires that cannot be satisfied throws an exception
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Desire d3 = new MockDesire();
        
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, d2),
                        new MockBindRule(d2, d3));
        
        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
    }
    
    @Test(expected=UnresolvableDependencyException.class)
    public void testNoBindRulesFail() throws Exception {
        // Test that not providing applicable bind rules will throw an exception,
        // even if other bind rules are given
        Desire d1 = new MockDesire();
        Desire d2 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        
        Desire b2 = new MockDesire();
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.put(ContextPattern.any(),
                     new MockBindRule(d2, b2));

        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
    }

    @Test(expected=UnresolvableDependencyException.class)
    public void testNonLeafSatisfiableDesireFail() throws Exception {
        // Test that a chain of desires, where an intermediate desire is
        // satisfiable but the leaf node is not, still throws an exception
        Desire d1 = new MockDesire();
        Satisfaction s1 = new MockSatisfaction(A.class, Arrays.asList(d1));
        Satisfaction s2 = new MockSatisfaction(B.class);
        
        Desire b1 = new MockDesire(s2);
        Desire b2 = new MockDesire();
        
        ImmutableListMultimap.Builder<ContextMatcher, BindRule> bindings = ImmutableListMultimap.builder();
        bindings.putAll(ContextPattern.any(),
                        new MockBindRule(d1, b1),
                        new MockBindRule(b1, b2));

        Desire rootDesire = new MockDesire(s1);
        DependencySolver r = createSolver(bindings.build());
        r.resolve(rootDesire);
    }
    
    // Find the node for s connected to p by the given desire, d
    private DAGNode<CachedSatisfaction, DesireChain> getNode(DAGNode<CachedSatisfaction, DesireChain> graph, Satisfaction s, Desire d) {
        for (DAGEdge<CachedSatisfaction, DesireChain> e: graph.getOutgoingEdges()) {
            if (e.getLabel().getInitialDesire().equals(d) && e.getTail().getLabel().getSatisfaction().equals(s)) {
                return e.getTail();
            }
        }
        return null;
    }
    
    private DAGNode<CachedSatisfaction, DesireChain> getNode(DAGNode<CachedSatisfaction, DesireChain> g, CachedSatisfaction s) {
        return Iterables.find(g.getReachableNodes(),
                              DAGNode.labelMatches(Predicates.equalTo(s)),
                              null);
    }
    
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Qual {
        int value();
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
