/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht.graph;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.junit.Test;

import javax.annotation.Nullable;
import java.util.HashMap;
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

    @Test
    public void testReplaceDualUseNode() {
        DAGNode<String,String> foo, fooP, bar, blatz, graph;
        foo = DAGNode.singleton("foo");
        // this node should be replaced once, not twice
        fooP = DAGNode.<String,String>newBuilder("foo'")
                .addEdge(foo, "-> foo")
                .build();
        bar = DAGNode.<String,String>newBuilder("bar")
                     .addEdge(fooP, "bar -> foo")
                     .build();
        blatz = DAGNode.<String,String>newBuilder("blatz")
                       .addEdge(fooP, "blatz -> foo")
                       .build();
        graph = DAGNode.<String,String>newBuilder("graph")
                .addEdge(bar, "-> bar")
                .addEdge(blatz, "-> blatz")
                .build();

        DAGNode<String,String> foo2 = DAGNode.singleton("foo2");
        HashMap<DAGNode<String,String>,DAGNode<String,String>> memory = Maps.newHashMap();
        DAGNode<String,String> replaced = graph.replaceNode(foo, foo2, memory);
        assertThat(replaced, not(sameInstance(graph)));
        // if we have > 5 nodes, then fooP was duplicated!
        assertThat(replaced.getReachableNodes(),
                   hasSize(5));
        assertThat(foo2, isIn(replaced.getReachableNodes()));
        assertThat(foo, not(isIn(replaced.getReachableNodes())));
        assertThat(fooP, not(isIn(replaced.getReachableNodes())));
        assertThat(replaced.getSortedNodes().get(0),
                   equalTo(foo2));
        assertThat(replaced.getSortedNodes().get(1).getLabel(),
                   equalTo("foo'"));
    }

    @Test
    public void testTransformEdgeNoop() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> graph = DAGNode.<String,String>newBuilder("graph")
                                              .addEdge(foo, "wombat")
                                              .build();
        DAGNode<String,String> g2 = graph.transformEdges((Function) Functions.constant(null));
        assertThat(g2, sameInstance(graph));
    }

    @Test
    public void testTransformEdgeRewrite() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> graph = DAGNode.<String,String>newBuilder("graph")
                                              .addEdge(foo, "wombat")
                                              .build();
        DAGNode<String,String> g2 =
                graph.transformEdges((Function) Functions.constant(DAGEdge.create(graph, foo,"hairball")));
        assertThat(g2, not(sameInstance(graph)));
        assertThat(g2.getOutgoingEdge(foo, "hairball"),
                   notNullValue());
    }

    @Test
    public void testTransformDeeperEdge() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.singleton("bar");
        DAGNode<String,String> wombat = DAGNode.<String,String>newBuilder("wombat")
                                               .addEdge(foo, "wombat")
                                               .build();
        DAGNode<String,String> graph = DAGNode.<String,String>newBuilder("graph")
                                              .addEdge(wombat, "wumpus")
                                              .addEdge(bar, "woozle")
                                              .build();
        DAGNode<String,String> g2 =
                graph.transformEdges(new Function<DAGEdge<String, String>, DAGEdge<String, String>>() {
                    @Nullable
                    @Override
                    public DAGEdge<String, String> apply(@Nullable DAGEdge<String, String> input) {
                        if (input != null && input.getLabel().equals("woozle")) {
                            return DAGEdge.create(input.getHead(), input.getTail(), "hatrack");
                        } else {
                            return null;
                        }
                    }
                });
        assertThat(g2, not(sameInstance(graph)));
        assertThat(g2.getOutgoingEdge(bar, "hatrack"),
                   notNullValue());
        assertThat(g2.getOutgoingEdge(bar, "woozle"),
                   nullValue());
        assertThat(g2.getOutgoingEdge(wombat, "wumpus"),
                   notNullValue());
    }

    @Test
    public void testFindBFSSingletonYes() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> needle = foo.findNodeBFS(Predicates.<DAGNode<String, String>>alwaysTrue());
        assertThat(needle, sameInstance(foo));
    }

    @Test
    public void testFindBFSSingletonNo() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> needle = foo.findNodeBFS(Predicates.<DAGNode<String, String>>alwaysFalse());
        assertThat(needle, nullValue());
    }

    @Test
    public void testFindBFSChildMatches() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo, "foo")
                                            .build();
        DAGNode<String,String> needle =
                bar.findNodeBFS(DAGNode.labelMatches(Predicates.equalTo("foo")));
        assertThat(needle, sameInstance(foo));
    }

    @Test
    public void testFindBFSFirstNode() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo, "foo")
                                            .build();
        DAGNode<String,String> needle = bar.findNodeBFS(Predicates.<DAGNode<String, String>>alwaysTrue());
        assertThat(needle, sameInstance(bar));
    }

    @Test
    public void testFindBFSFirstFound() {
        DAGNode<String,String> foo1 = DAGNode.singleton("foo");
        DAGNode<String,String> foo2 = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo1, "hello")
                                            .build();
        DAGNode<String,String> bam = DAGNode.<String,String>newBuilder("bam")
                                            .addEdge(bar, "wombat")
                                            .addEdge(foo2, "goodbye")
                                            .build();
        DAGNode<String,String> needle = bam.findNodeBFS(DAGNode.labelMatches(Predicates.equalTo("foo")));
        assertThat(needle, sameInstance(foo2));
    }

    @Test
    public void testEdgeBFSSingleYes() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo, "-> foo")
                                            .build();
        DAGEdge<String,String> edge = bar.findEdgeBFS(Predicates.alwaysTrue());
        // we should have just found the only edge
        assertThat(edge, isIn(bar.getOutgoingEdges()));
    }

    @Test
    public void testEdgeBFSSingleNo() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo, "-> foo")
                                            .build();
        DAGEdge<String,String> edge = bar.findEdgeBFS(Predicates.alwaysFalse());
        // no edge to find
        assertThat(edge, nullValue());
    }

    @Test
    public void testEdgeBFSByName() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> baz = DAGNode.singleton("baz");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo, "-> foo")
                                            .addEdge(baz, "-> baz")
                                            .build();
        DAGEdge<String,String> edge = bar.findEdgeBFS(e -> e.getLabel().equals("-> baz"));
        // we should find the correct edge
        assertThat(edge, isIn(bar.getOutgoingEdges()));
        assertThat(edge.getTail(), equalTo(baz));
        assertThat(edge.getLabel(), equalTo("-> baz"));
    }

    @Test
    public void testEdgeBFSFirst() {
        DAGNode<String,String> foo = DAGNode.singleton("foo");
        DAGNode<String,String> bar = DAGNode.<String,String>newBuilder("bar")
                                            .addEdge(foo, "hello")
                                            .build();
        DAGNode<String,String> bam = DAGNode.<String,String>newBuilder("bam")
                                            .addEdge(bar, "wombat")
                                            .addEdge(foo, "goodbye")
                                            .build();
        DAGEdge<String,String> edge = bam.findEdgeBFS(e -> e.getTail().equals(foo));
        // we should find the first edge
        assertThat(edge.getLabel(),
                   equalTo("goodbye"));
    }
}
