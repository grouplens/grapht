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

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    private final DAGNode<V,E> head;
    @NotNull
    private final DAGNode<V,E> tail;
    @NotNull
    @SuppressWarnings("squid:S1948") // serializable warning; edge is serializable iff its label type is
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
    @NotNull
    public DAGNode<V,E> getHead() {
        return head;
    }

    /**
     * Get the edge's tail (ending node).
     * @return The edge's tail.
     */
    @NotNull
    public DAGNode<V,E> getTail() {
        return tail;
    }

    /**
     * Get the edge's label.
     * @return The edge's label.
     */
    @NotNull
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
}