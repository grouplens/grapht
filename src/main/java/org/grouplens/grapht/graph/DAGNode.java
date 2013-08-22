package org.grouplens.grapht.graph;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.Set;

/**
 * A node in a (rooted) DAG.  Since DAGs are rooted, a full graph is just represented by its root
 * node.  Two nodes are equal if their labels and adjacency lists are equal (therefore, they are
 * equal if and only if they are the roots of equal graphs).
 *
 * <p>Nodes and edges may not have null labels.  There <em>may</em> be multiple edges from one
 * node to another, so long as those edges have distinct labels.
 *
 * <p>Nodes know about all nodes reachable from them, and the edges connecting those nodes.
 *
 * <p>DAGs and their nodes are immutable.  You can build them using a {@linkplain DAGNodeBuilder builder},
 * obtained from {@link #newBuilder(Object)}.
 *
 * @param <V> The type of node (vertex) labels.
 * @param <E> The type of edge labels.
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Immutable
public class DAGNode<V,E> implements Serializable {
    private static final long serialVersionUID = 1L;

    private final V label;
    private final ImmutableSet<DAGEdge<V,E>> outgoingEdges;

    /**
     * Create a new DAG node with no outgoing edges.
     * @param label The node label.
     * @param <V> The type of node labels.
     * @param <E> The type of edge labels.
     */
    public static <V,E> DAGNode<V,E> singleton(V label) {
        Preconditions.checkNotNull(label, "node label");
        return new DAGNode<V,E>(label, ImmutableSet.<Pair<DAGNode<V, E>, E>>of());
    }

    /**
     * Construct a new DAG node builder.
     * @param <V> The type of node labels.
     * @param <E> The type of edge labels.
     * @return The DAG node builder.
     */
    public static <V,E> DAGNodeBuilder<V,E> newBuilder() {
        return new DAGNodeBuilder<V, E>();
    }

    /**
     * Construct a new DAG node builder.
     * @param label The node label.
     * @param <V> The type of node labels.
     * @param <E> The type of edge labels.
     * @return The DAG node builder.
     */
    public static <V,E> DAGNodeBuilder<V,E> newBuilder(V label) {
        return new DAGNodeBuilder<V, E>(label);
    }

    /**
     * Construct a new DAG node.
     * @param lbl The label.
     * @param edges The edges.  This takes pairs, not actual edge objects, because the edge objects
     *              need to be constructed within the constructor in order to create the circular
     *              references back to the head nodes properly.
     */
    DAGNode(V lbl, Iterable<Pair<DAGNode<V,E>,E>> edges) {
        label = lbl;
        ImmutableSet.Builder<DAGEdge<V,E>> bld = ImmutableSet.builder();
        for (Pair<DAGNode<V,E>,E> pair: edges) {
            DAGEdge<V,E> edge = new DAGEdge<V, E>(this, pair.getLeft(), pair.getRight());
            bld.add(edge);
        }
        outgoingEdges = bld.build();
    }

    /**
     * Get the label for this node.
     * @return The node's label.
     */
    @Nonnull
    public V getLabel() {
        return label;
    }

    /**
     * Get the outgoing edges of this node.
     * @return The outgoing edges of the node.
     */
    @Nonnull
    public Set<DAGEdge<V,E>> getOutgoingEdges() {
        return outgoingEdges;
    }
}
