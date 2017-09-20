/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht;

import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.solver.DependencySolver;
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
    public void testSimpleRewriteNoTrigger() throws ResolutionException {
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
        DAGNode<Component, Dependency> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        DAGNode<Component, Dependency> graph2 = initial.rewrite(graph);
        assertThat(graph2, sameInstance(graph));
    }

    @Test
    public void testRewriteToIdentical() throws ResolutionException {
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
        DAGNode<Component, Dependency> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        DAGNode<Component, Dependency> graph2 = initial.rewrite(graph);
        // should trigger a rewrite, but the graph should be unchanged
        assertThat(graph2, sameInstance(graph));
    }

    @Test
    public void testRewriteDependency() throws ResolutionException {
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
        DAGNode<Component, Dependency> graph = initial.getGraph();
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

        DAGNode<Component, Dependency> graph2 = rewriter.rewrite(graph);
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
    public void testRewriteNoTrigger() throws ResolutionException {
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
        DAGNode<Component, Dependency> graph = initial.getGraph();
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

        DAGNode<Component, Dependency> graph2 = rewriter.rewrite(graph);
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
    public void testRewriteFixed() throws ResolutionException {
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
        DAGNode<Component, Dependency> graph = initial.getGraph();

        BindingFunctionBuilder config2 = new BindingFunctionBuilder();
        config2.getRootContext()
               .bind(I2.class)
               .to(B.class);
        DependencySolver rewriter =
                DependencySolver.newBuilder()
                                .addBindingFunction(config2.build(BindingFunctionBuilder.RuleSet.EXPLICIT), true)
                                .build();

        DAGNode<Component, Dependency> graph2 = rewriter.rewrite(graph);
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
