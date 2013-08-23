package org.grouplens.grapht.graph;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TestDAGNode {
    @Test
    public void testBasicLabel() {
        DAGNode<String,String> node = DAGNode.singleton("foo");
        assertThat(node, notNullValue());
        assertThat(node.getLabel(), equalTo("foo"));
        assertThat(node.getOutgoingEdges(), hasSize(0));
    }

    @Test
    public void testSingleEdge() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNodeBuilder<String,String> bld = DAGNode.newBuilder("bar");
        DAGNode<String,String> bar = bld.addEdge(foo, "wombat")
                                        .build();

        assertThat(bar.getLabel(), equalTo("bar"));
        assertThat(bar.getOutgoingEdges(), hasSize(1));
        DAGEdge<String,String> e = bar.getOutgoingEdges().iterator().next();
        assertThat(e.getLabel(), equalTo("wombat"));
        assertThat(e.getTail().getLabel(), equalTo("foo"));
        assertThat(e.getTail().getOutgoingEdges(), hasSize(0));
    }

    @Test
    public void testGetReverseEdge() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNodeBuilder<String,String> bld = DAGNode.newBuilder("bar");
        bld.addEdge(foo, "wombat");
        DAGNode<String,String> bar = bld.build();

        assertThat(bar.getIncomingEdges(foo),
                   contains(DAGEdge.create(bar, foo, "wombat")));
    }

    @Test
    public void testGetTwoReverseEdges() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");

        DAGNodeBuilder<String,String> bld = DAGNode.newBuilder("bar");
        DAGNode<String,String> bar = bld.addEdge(foo, "wombat").build();

        bld = DAGNode.newBuilder("blatz");
        DAGNode<String,String> blatz = bld.addEdge(foo, "skunk").build();

        bld = DAGNode.newBuilder("head");
        DAGNode<String,String> head = bld.addEdge(bar, "wumpus")
                                         .addEdge(blatz, "woozle")
                                         .build();

        assertThat(head.getOutgoingEdges(),
                   hasSize(2));

        assertThat(head.getIncomingEdges(foo),
                   containsInAnyOrder(DAGEdge.create(bar, foo, "wombat"),
                                      DAGEdge.create(blatz, foo, "skunk")));
    }
}
