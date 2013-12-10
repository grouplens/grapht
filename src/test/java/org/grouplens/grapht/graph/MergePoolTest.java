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
package org.grouplens.grapht.graph;

import com.google.common.collect.Lists;
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
                   anyOf(hasItem(sameInstance(node)),
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
