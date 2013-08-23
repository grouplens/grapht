package org.grouplens.grapht.graph;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.util.HashSet;
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

    @Nonnull
    private final V label;
    @Nonnull
    private final ImmutableSet<DAGEdge<V,E>> outgoingEdges;
    private transient volatile int hashCode;
    @Nullable
    private transient volatile ImmutableSetMultimap<DAGNode<V,E>,DAGEdge<V,E>> reverseEdgeCache;

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

    /**
     * Get a multimap of incoming edges.  For each node reachable from this node, the map will
     * contain each of its incoming edges (also reachable from this graph).
     *
     * @return The reverse edge map.
     */
    @Nonnull
    public SetMultimap<DAGNode<V,E>,DAGEdge<V,E>> getIncomingEdgeMap() {
        if (reverseEdgeCache == null) {
            ImmutableSetMultimap.Builder<DAGNode<V,E>,DAGEdge<V,E>> bld = ImmutableSetMultimap.builder();
            for (DAGEdge<V,E> nbr: outgoingEdges) {
                bld.put(nbr.getTail(), nbr);
                bld.putAll(nbr.getTail().getIncomingEdgeMap());
            }
            reverseEdgeCache = bld.build();
        }
        return reverseEdgeCache;
    }

    /**
     * Get the incoming edges to a node reachable from this node.
     * @return The set of incoming edges, or an empty set if the node is not reachable.
     */
    @Nonnull
    public Set<DAGEdge<V,E>> getIncomingEdges(DAGNode<V,E> node) {
        return getIncomingEdgeMap().get(node);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hcb.append(label);
            int edgeHash = 0;
            for (DAGEdge<V,E> edge: outgoingEdges) {
                edgeHash += 37 * (37 * 17 + edge.getTail().hashCode()) + edge.getLabel().hashCode();
            }
            hcb.append(edgeHash);
            hashCode = hcb.toHashCode();
        }
        return hashCode;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This does a deep structural comparison of the two DAG nodes.  Comparing DAG nodes for
     * equality is therefore expensive, unless they are the same object or have different labels.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof DAGNode) {
            DAGNode<?,?> on = (DAGNode) o;
            if (!label.equals(on.label)) {
                return false;
            } else if (outgoingEdges.size() != on.outgoingEdges.size()) {
                return false;
            }

            Function<DAGEdge,Pair<DAGNode,Object>> edgePair = new Function<DAGEdge, Pair<DAGNode, Object>>() {
                @Nullable
                @Override
                public Pair<DAGNode, Object> apply(@Nullable DAGEdge input) {
                    if (input == null) {
                        return null;
                    } else {
                        return Pair.of(input.getTail(), input.getLabel());
                    }
                }
            };

            HashSet<Pair<DAGNode, Object>> out =
                    Sets.newHashSet(Iterables.transform(outgoingEdges, edgePair));
            HashSet<Pair<DAGNode, Object>> otherOut
                    = Sets.newHashSet(Iterables.transform(on.outgoingEdges, edgePair));
            return out.equals(otherOut);
        } else {
            return false;
        }
    }
}
