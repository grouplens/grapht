package org.grouplens.grapht;

import org.grouplens.grapht.graph.DAGNode;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.CachedSatisfaction;
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
    public void testNoopRewrite() throws SolverException {
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
        initial.resolve(config.getSPI().desire(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        DAGNode<CachedSatisfaction, DesireChain> graph2 = initial.rewrite(graph);
        assertThat(graph2, sameInstance(graph));
    }

    @Test
    public void testRewriteInPlace() throws SolverException {
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
        initial.resolve(config.getSPI().desire(null, I.class, false));
        DAGNode<CachedSatisfaction, DesireChain> graph = initial.getGraph();
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        DAGNode<CachedSatisfaction, DesireChain> graph2 = initial.rewrite(graph);
        // should trigger a rewrite, but the graphs should be equivalent
        assertThat(graph2, not(sameInstance(graph)));
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
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
        initial.resolve(config.getSPI().desire(null, I.class, false));
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
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        assertThat(graph.getOutgoingEdges().iterator().next()
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
        initial.resolve(config.getSPI().desire(null, I.class, false));
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
        assertThat(graph.getOutgoingEdges(), hasSize(1));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) C.class));
        assertThat(graph.getOutgoingEdges().iterator().next()
                        .getTail().getOutgoingEdges().iterator().next()
                        .getTail().getLabel().getSatisfaction().getErasedType(),
                   equalTo((Class) W.class));
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
