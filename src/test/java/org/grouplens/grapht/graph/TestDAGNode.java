package org.grouplens.grapht.graph;

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

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
        assertThat(node.getReachableNodes(),
                   contains(node));
        assertThat(node.getOutgoingEdgeWithLabel("wombat"),
                   nullValue());
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
        assertThat(bar.getReachableNodes(),
                   containsInAnyOrder(foo, bar));
        assertThat(bar.getSortedNodes(),
                   contains(foo, bar));
        assertThat(bar.getOutgoingEdgeWithLabel("wombat").getTail(),
                   equalTo(foo));
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
        assertThat(head.getReachableNodes(),
                   containsInAnyOrder(foo, bar, blatz, head));
    }

    @Test
    public void testReplaceSingleNode() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.singleton("bar");
        Map<DAGNode<String,String>,DAGNode<String,String>> mem = Maps.newHashMap();
        DAGNode<String,String> replaced = foo.replaceNode(foo, bar, mem);
        assertThat(replaced, equalTo(bar));
        assertThat(mem, hasEntry(foo, bar));
    }

    @Test
    public void testReplaceAbsentNode() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.singleton("bar");
        DAGNode<String,String> blatz = DAGNode.singleton("blatz");
        Map<DAGNode<String,String>,DAGNode<String,String>> mem = Maps.newHashMap();
        DAGNode<String,String> replaced = foo.replaceNode(blatz, bar, mem);
        assertThat(replaced, equalTo(foo));
        assertThat(mem.size(), equalTo(0));
    }

    @Test
    public void testReplaceEdgeNode() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.singleton("bar");
        DAGNode<String,String> graph = DAGNode.<String,String>newBuilder("graph")
                                              .addEdge(foo, "wombat")
                                              .build();
        Map<DAGNode<String,String>,DAGNode<String,String>> mem = Maps.newHashMap();
        DAGNode<String,String> replaced = graph.replaceNode(foo, bar, mem);
        assertThat(replaced, not(equalTo(graph)));
        assertThat(mem, hasEntry(foo, bar));
        assertThat(mem, hasEntry(graph, replaced));
        assertThat(replaced.getAdjacentNodes(), contains(bar));
    }
}
