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

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
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
        edges = Sets.newHashSet();
    }

    /**
     * Set the node's label.
     * @param lbl The node's label.
     * @return The builder (for chaining).
     */
    @Nonnull
    public DAGNodeBuilder<V,E> setLabel(@Nonnull V lbl) {
        Preconditions.checkNotNull(lbl, "node label");
        label = lbl;
        return this;
    }

    /**
     * Add an edge.
     * @param target The target node.
     * @param label The edge label.
     * @return The builder (for chaining).
     */
    @Nonnull
    public DAGNodeBuilder<V,E> addEdge(@Nonnull DAGNode<V,E> target,
                                       @Nonnull E label) {
        Preconditions.checkNotNull(target, "edge target");
        Preconditions.checkNotNull(label, "edge label");
        return addEdge(Pair.of(target, label));
    }

    /**
     * Add an edge.
     * @param edge The target node and label for the edge.
     * @return The builder (for chaining).
     */
    @Nonnull
    public DAGNodeBuilder<V,E> addEdge(Pair<DAGNode<V,E>,E> edge) {
        Preconditions.checkNotNull(edge, "edge");
        Preconditions.checkNotNull(edge.getLeft(), "edge target");
        Preconditions.checkNotNull(edge.getRight(), "edge label");
        edges.add(edge);
        return this;
    }

    @Nonnull
    public DAGNode<V,E> build() {
        Preconditions.checkState(label != null, "no node label set");
        return new DAGNode<V,E>(label, edges);
    }

    @Override
    public String toString() {
        return String.format("node builder with label %s and %d edges", label, edges.size());
    }
}
