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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import org.jetbrains.annotations.NotNull;
import java.util.Set;

/**
 * A builder for {@linkplain DAGNode DAG nodes}.  You can create one with {@link DAGNode#newBuilder()}
 * or {@link DAGNode#newBuilder(Object)}.
 *
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DAGNodeBuilder<V,E> {
    private V label;
    private Set<Pair<DAGNode<V,E>,E>> edges;

    public DAGNodeBuilder() {
        this(null);
    }

    public DAGNodeBuilder(V lbl) {
        label = lbl;
        edges = Sets.newLinkedHashSet();
    }

    /**
     * Set the node's label.
     * @param lbl The node's label.
     * @return The builder (for chaining).
     */
    @NotNull
    public DAGNodeBuilder<V,E> setLabel(@NotNull V lbl) {
        Preconditions.checkNotNull(lbl, "node label");
        label = lbl;
        return this;
    }

    /**
     * Get the label set for this node.
     * @return The label currently set for the node builder.
     */
    public V getLabel() {
        return label;
    }

    /**
     * Add an edge.
     * @param target The target node.
     * @param label The edge label.
     * @return The builder (for chaining).
     */
    @NotNull
    public DAGNodeBuilder<V,E> addEdge(@NotNull DAGNode<V,E> target,
                                       @NotNull E label) {
        Preconditions.checkNotNull(target, "edge target");
        Preconditions.checkNotNull(label, "edge label");
        return addEdge(Pair.of(target, label));
    }

    /**
     * Add an edge.
     * @param edge The target node and label for the edge.
     * @return The builder (for chaining).
     */
    @NotNull
    public DAGNodeBuilder<V,E> addEdge(Pair<DAGNode<V,E>,E> edge) {
        Preconditions.checkNotNull(edge, "edge");
        Preconditions.checkNotNull(edge.getLeft(), "edge target");
        Preconditions.checkNotNull(edge.getRight(), "edge label");
        edges.add(edge);
        return this;
    }

    /**
     * Get the set of edges.  This set is live, and can be used to modify the edges that will
     * be put in the final builder.
     *
     * @return The set of edges.
     */
    @NotNull
    public Set<Pair<DAGNode<V,E>,E>> getEdges() {
        return edges;
    }

    @NotNull
    public DAGNode<V,E> build() {
        Preconditions.checkState(label != null, "no node label set");
        return new DAGNode<V,E>(label, edges);
    }

    @Override
    public String toString() {
        return String.format("node builder with label %s and %d edges", label, edges.size());
    }
}
