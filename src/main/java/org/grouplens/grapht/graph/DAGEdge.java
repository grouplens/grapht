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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Edges in DAGs.  These arise from building nodes with a {@link DAGNodeBuilder}.
 *
 * <p>Two edges are equal if they connect the same pair of nodes and have the same label.
 *
 * @param <V> The type of node labels.
 * @param <E> The type of edge labels.
 * @see DAGNode
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DAGEdge<V,E> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final DAGNode<V,E> head;
    @Nonnull
    private final DAGNode<V,E> tail;
    @Nonnull
    private final E label;
    private transient volatile int hashCode;

    public static <V,E> DAGEdge<V,E> create(DAGNode<V,E> head, DAGNode<V,E> tail, E label) {
        return new DAGEdge<V,E>(head, tail, label);
    }

    DAGEdge(DAGNode<V,E> hd, DAGNode<V,E> tl, E lbl) {
        assert hd != null;
        assert tl != null;
        assert lbl != null;
        head = hd;
        tail = tl;
        label = lbl;
    }

    /**
     * Get the edge's head (starting node).
     * @return The edge's head.
     */
    @Nonnull
    public DAGNode<V,E> getHead() {
        return head;
    }

    /**
     * Get the edge's tail (ending node).
     * @return The edge's tail.
     */
    @Nonnull
    public DAGNode<V,E> getTail() {
        return tail;
    }

    /**
     * Get the edge's label.
     * @return The edge's label.
     */
    @Nonnull
    public E getLabel() {
        return label;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = new HashCodeBuilder().append(head).append(tail).append(label).toHashCode();
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof DAGEdge) {
            DAGEdge<?,?> oe = (DAGEdge<?,?>) o;
            return head.equals(oe.getHead())
                   && tail.equals(oe.getTail())
                   && label.equals(oe.getLabel());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("edge ")
          .append(label)
          .append(" from ")
          .append(head)
          .append(" to ")
          .append(tail);
        return sb.toString();
    }

    public static <E> Predicate<DAGEdge<?,E>> labelMatches(final Predicate<E> pred) {
        return new Predicate<DAGEdge<?, E>>() {
            @Override
            public boolean apply(@Nullable DAGEdge<?, E> input) {
                E label = input == null ? null : input.getLabel();
                return pred.apply(label);
            }
        };
    }

    public static <V,E> Predicate<DAGEdge<V,E>> tailMatches(final Predicate<DAGNode<V,E>> pred) {
        return new Predicate<DAGEdge<V, E>>() {
            @Override
            public boolean apply(@Nullable DAGEdge<V, E> input) {
                DAGNode<V,E> tail = input == null ? null : input.getTail();
                return pred.apply(tail);
            }
        };
    }

    public static <V,E> Function<DAGEdge<V,E>,DAGNode<V,E>> extractTail() {
        return new Function<DAGEdge<V, E>, DAGNode<V, E>>() {
            @Nullable
            @Override
            public DAGNode<V, E> apply(@Nullable DAGEdge<V, E> input) {
                return input == null ? null : input.getTail();
            }
        };
    }

    public static <V,E> Predicate<DAGEdge<V,E>> headMatches(final Predicate<DAGNode<V,E>> pred) {
        return new Predicate<DAGEdge<V, E>>() {
            @Override
            public boolean apply(@Nullable DAGEdge<V, E> input) {
                DAGNode<V,E> head = input == null ? null : input.getHead();
                return pred.apply(head);
            }
        };
    }

    public static <V,E> Function<DAGEdge<V,E>,DAGNode<V,E>> extractHead() {
        return new Function<DAGEdge<V, E>, DAGNode<V, E>>() {
            @Nullable
            @Override
            public DAGNode<V, E> apply(@Nullable DAGEdge<V, E> input) {
                return input == null ? null : input.getHead();
            }
        };
    }

    /**
     * Transform an edge.  This function does not further transform the nodes, so if the edge is
     * known as the outgoing edge of a node, that won't be fixed.  Mostly useful for bulk operations
     * on a bunch of edges, if you have them lying around.
     *
     * @param func The node transformation function.  If this function returns null, that is treated
     *             as equivalent to the identity function.
     * @param <V> The type of vertices.
     * @param <E> The type of edges.
     * @return A function over edges.
     */
    public static <V,E> Function<DAGEdge<V,E>,DAGEdge<V,E>> transformNodes(final Function<DAGNode<V,E>,DAGNode<V,E>> func) {
        return new Function<DAGEdge<V, E>, DAGEdge<V, E>>() {
            @Nullable
            @Override
            public DAGEdge<V, E> apply(@Nullable DAGEdge<V, E> input) {
                if (input == null) {
                    return null;
                }
                DAGNode<V,E> nt, nh;
                nt = func.apply(input.getTail());
                nh = func.apply(input.getHead());
                if (nt == null) {
                    nt = input.getTail();
                }
                if (nh == null) {
                    nh = input.getHead();
                }
                if (!nt.equals(input.getTail()) || !nh.equals(input.getHead())) {
                    return create(nh, nt, input.getLabel());
                } else {
                    return input;
                }
            }
        };
    }
}