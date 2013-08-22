package org.grouplens.grapht.graph;

import com.google.common.base.Preconditions;
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
        edges.add(Pair.of(target, label));
        return this;
    }

    @Nonnull
    public DAGNode<V,E> build() {
        Preconditions.checkState(label != null, "no node label set");
        return new DAGNode<V,E>(label, edges);
    }
}
