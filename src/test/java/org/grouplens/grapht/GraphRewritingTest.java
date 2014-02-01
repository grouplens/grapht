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
package org.grouplens.grapht;

import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.reflect.CachedSatisfaction;
import org.grouplens.grapht.reflect.Desires;
import org.junit.Test;

import javax.inject.Inject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test the dependency solver's graph rewriting capabilities.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class GraphRewritingTest {
    @Test
    public void testSimpleRewriteNoTrigger() throws SolverException {
        BindingFunctionBuilder config = new BindingFunctionBuilder();
        config.getRootContext()
                .bind(I.class)
                .to(C.class);
        config.getRootContext()
              .bind(I2.class)
              .to(A.class);
        DependencySolver initial =
                DependencySolver.newBuilder()
                                .addBindingFunction(config.build(BindingFunctionBuilder.RuleSet.EXPLICIT), false)
                                .build();
        initial.resolve(Desires.create(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        DAGNode<CachedSatisfaction, DesireChain> graph2 = initial.rewrite(graph);
        assertThat(graph2, sameInstance(graph));
    }

    @Test
    public void testRewriteToIdentical() throws SolverException {
        BindingFunctionBuilder config = new BindingFunctionBuilder();
        config.getRootContext()
              .bind(I.class)
              .to(C.class);
        config.getRootContext()
              .bind(I2.class)
              .to(A.class);
        DependencySolver initial =
                DependencySolver.newBuilder()
                                .addBindingFunction(config.build(BindingFunctionBuilder.RuleSet.EXPLICIT), true)
                                .build();
        initial.resolve(Desires.create(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        DAGNode<CachedSatisfaction, DesireChain> graph2 = initial.rewrite(graph);
        // should trigger a rewrite, but the graph should be unchanged
        assertThat(graph2, sameInstance(graph));
    }

    @Test
    public void testRewriteDependency() throws SolverException {
        BindingFunctionBuilder config = new BindingFunctionBuilder();
        config.getRootContext()
              .bind(I.class)
              .to(C.class);
        config.getRootContext()
              .bind(I2.class)
              .to(A.class);
        DependencySolver initial =
                DependencySolver.newBuilder()
                                .addBindingFunction(config.build(BindingFunctionBuilder.RuleSet.EXPLICIT))
                                .build();
        initial.resolve(Desires.create(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) A.class));

        BindingFunctionBuilder config2 = new BindingFunctionBuilder();
        config2.getRootContext()
               .bind(I2.class)
               .to(B.class);
        DependencySolver rewriter =
                DependencySolver.newBuilder()
                                .addBindingFunction(config2.build(BindingFunctionBuilder.RuleSet.EXPLICIT), true)
                                .build();

        DAGNode<CachedSatisfaction, DesireChain> graph2 = rewriter.rewrite(graph);
        // should change the dependency
        assertThat(graph2, not(sameInstance(graph)));
        assertThat(graph2.getOutgoingEdges(), hasSize(1));
        assertThat(graph2.getOutgoingEdges().iterator().next()
                         .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        assertThat(graph2.getOutgoingEdges().iterator().next()
                         .getTail().getOutgoingEdges().iterator().next()
                         .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) B.class));
    }

    @Test
    public void testRewriteNoTrigger() throws SolverException {
        BindingFunctionBuilder config = new BindingFunctionBuilder();
        config.getRootContext()
              .bind(I.class)
              .to(C.class);
        config.getRootContext()
              .bind(I2.class)
              .to(A.class);
        DependencySolver initial =
                DependencySolver.newBuilder()
                                .addBindingFunction(config.build(BindingFunctionBuilder.RuleSet.EXPLICIT))
                                .build();
        initial.resolve(Desires.create(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) A.class));

        BindingFunctionBuilder trigger = new BindingFunctionBuilder();
        trigger.getRootContext()
               .bind(I2.class)
               .to(W.class);
        BindingFunctionBuilder rewriteDeps = new BindingFunctionBuilder();
        trigger.getRootContext()
               .bind(InputStream.class)
               .to(new ByteArrayInputStream("foo".getBytes()));
        DependencySolver rewriter =
                DependencySolver.newBuilder()
                                .addBindingFunction(trigger.build(BindingFunctionBuilder.RuleSet.EXPLICIT), true)
                                .addBindingFunction(rewriteDeps.build(BindingFunctionBuilder.RuleSet.EXPLICIT), false)
                                .build();

        DAGNode<CachedSatisfaction, DesireChain> graph2 = rewriter.rewrite(graph);
        // should change the dependency
        assertThat(graph2, not(sameInstance(graph)));
        assertThat(graph2.getOutgoingEdges(), hasSize(1));
        assertThat(graph2.getOutgoingEdges().iterator().next()
                         .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        assertThat(graph2.getOutgoingEdges().iterator().next()
                         .getTail().getOutgoingEdges().iterator().next()
                         .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) W.class));
    }

    @Test
    public void testRewriteFixed() throws SolverException {
        // based on testRewriteDependency, but with a fixed binding to prevent rewrite
        BindingFunctionBuilder config = new BindingFunctionBuilder();
        config.getRootContext()
              .bind(I.class)
              .to(C.class);
        config.getRootContext()
              .bind(I2.class)
              .fixed()
              .to(A.class);
        DependencySolver initial =
                DependencySolver.newBuilder()
                                .addBindingFunction(config.build(BindingFunctionBuilder.RuleSet.EXPLICIT))
                                .build();
        initial.resolve(Desires.create(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();

        BindingFunctionBuilder config2 = new BindingFunctionBuilder();
        config2.getRootContext()
               .bind(I2.class)
               .to(B.class);
        DependencySolver rewriter =
                DependencySolver.newBuilder()
                                .addBindingFunction(config2.build(BindingFunctionBuilder.RuleSet.EXPLICIT), true)
                                .build();

        DAGNode<CachedSatisfaction, DesireChain> graph2 = rewriter.rewrite(graph);
        // should be unchanged, because of fixed binding
        assertThat(graph2, sameInstance(graph));
    }

    public static interface I {}
    public static interface I2 {}
    public static class C implements I {
        final I2 plug;

        @Inject
        public C(I2 plug) {
            this.plug = plug;
        }
    }

    public static class A implements I2 {}
    public static class B implements I2 {}
    public static class W implements I2 {
        private final InputStream stream;

        @Inject
        public W(InputStream s) {
            stream = s;
        }
    }
}
