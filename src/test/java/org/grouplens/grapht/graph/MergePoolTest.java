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
package org.grouplens.grapht.graph;

import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class MergePoolTest {
    MergePool<String,String> pool;

    @Before
    public void createPool() {
        pool = MergePool.create();
    }

    @Test
    public void testSingletonNode() {
        DAGNode<String,String> node = DAGNode.singleton("foo");
        DAGNode<String,String> merged = pool.merge(node);
        // merging a singleton node should just return the node
        assertThat(merged, sameInstance(node));
    }

    @Test
    public void testReuseSingleton() {
        DAGNode<String,String> node = DAGNode.singleton("foo");
        DAGNode<String,String> node2 = DAGNode.singleton("foo");
        // simplify first so it's in the pool
        pool.merge(node);
        // and merge the second
        DAGNode<String,String> merged = pool.merge(node2);
        // the first node should have been reused
        assertThat(merged, sameInstance(node));
    }

    @Test
    public void testMergeDescendants() {
        DAGNode<String,String> node = DAGNode.singleton("foo");
        DAGNode<String,String> node2 = DAGNode.singleton("foo");
        DAGNode<String,String> root =
                DAGNode.<String,String>newBuilder("root")
                       .addEdge(node, "hello")
                       .addEdge(node2, "goodbye")
                       .build();

        DAGNode<String,String> merged = pool.merge(root);

        // now, node and node2 should be merged
        // new graph (since they're merged)
        assertThat(merged, not(sameInstance(root)));
        // only 2 nodes (root has 2 edges to same node)
        assertThat(merged.getReachableNodes(), hasSize(2));
        // two edges have same target
        List<DAGEdge<String,String>> nbrs =
                Lists.newArrayList(merged.getOutgoingEdges());
        // both edges have the same instance
        assertThat(nbrs.get(1).getTail(),
                   sameInstance(nbrs.get(0).getTail()));
        // it's one of the nodes we gave it
        assertThat(nbrs.get(0).getTail(),
                   anyOf(sameInstance(node),
                         sameInstance(node2)));

        // and a new singleton should be merged
        assertThat(pool.merge(DAGNode.<String, String>singleton("foo")),
                   anyOf(sameInstance(node),
                         sameInstance(node2)));

        // re-merging our old root should give us the same node
        assertThat(pool.merge(root), sameInstance(merged));
    }

    @Test
    public void testMergeWithChildren() {
        DAGNode<String,String> node = DAGNode.singleton("foo");
        DAGNode<String,String> p1 =
                DAGNode.<String,String>newBuilder("child")
                       .addEdge(node, "k1")
                       .build();
        DAGNode<String,String> node2 = DAGNode.singleton("foo");
        // second parent node. it will have different label to test that labels are ignored
        DAGNode<String,String> p2 =
                DAGNode.<String,String>newBuilder("child")
                       .addEdge(node, "k2")
                       .build();
        DAGNode<String,String> root =
                DAGNode.<String,String>newBuilder("root")
                       .addEdge(p1, "hello")
                       .addEdge(p2, "fish")
                       .build();

        DAGNode<String,String> merged = pool.merge(root);

        // now, p1 and p2 should be merged
        // new graph (since they're merged)
        assertThat(merged, not(sameInstance(root)));
        // only 3 nodes (root has 2 edges to now-merged nodes)
        assertThat(merged.getReachableNodes(), hasSize(3));
        // it has one of the leaves
        assertThat(merged.getReachableNodes(),
                   (Matcher) anyOf(hasItem(sameInstance(node)),
                                   hasItem(sameInstance(node2))));
        // two edges have same target
        List<DAGEdge<String,String>> nbrs =
                Lists.newArrayList(merged.getOutgoingEdges());
        // both edges have the same instance
        assertThat(nbrs.get(1).getTail(),
                   sameInstance(nbrs.get(0).getTail()));

        // and a new singleton should be merged
        assertThat(pool.merge(DAGNode.<String, String>singleton("foo")),
                   anyOf(sameInstance(node),
                         sameInstance(node2)));

        // re-merging a child should give us a previously-merged node
        assertThat(pool.merge(p2), isIn(merged.getReachableNodes()));
        assertThat(pool.merge(p1), isIn(merged.getReachableNodes()));
    }
}
