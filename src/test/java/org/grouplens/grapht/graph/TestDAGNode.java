package org.grouplens.grapht.graph;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
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
}
