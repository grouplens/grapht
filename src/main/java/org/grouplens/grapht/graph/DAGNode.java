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

import com.google.common.base.*;
import com.google.common.collect.*;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.*;

/**
 * A node in a (rooted) DAG.  Since DAGs are rooted, a full graph is just represented by its root
 * node.  Nodes are compared using reference equality, so distinct nodes do not compare equal even
 * if they have identical labels and edge sets.
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

    @NotNull
    @SuppressWarnings("squid:S1948") // serializable warning; node is serializable iff its label type is
    private final V label;
    @NotNull
    private final ImmutableSet<DAGEdge<V,E>> outgoingEdges;

    private transient Supplier<SetMultimap<DAGNode<V,E>,DAGEdge<V,E>>> reverseEdgeCache;
    private transient Supplier<Set<DAGNode<V,E>>> reachableNodeCache;
    private transient Supplier<List<DAGNode<V,E>>> topologicalSortCache;

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
     * Create a new builder initialized to build a copy of the specified node.
     * @param node The node to copy.
     * @param <V> The type of node labels.
     * @param <E> The type of edge labels.
     * @return A new builder initialized with the labels and edges of {@code node}.
     */
    public static <V,E> DAGNodeBuilder<V,E> copyBuilder(DAGNode<V,E> node) {
        DAGNodeBuilder<V,E> bld = newBuilder(node.getLabel());
        for (DAGEdge<V,E> edge: node.getOutgoingEdges()) {
            bld.addEdge(edge.getTail(), edge.getLabel());
        }
        return bld;
    }

    /**
     * Construct a new DAG node.
     * @param lbl The label.
     * @param edges The edges.  This takes pairs, not actual edge objects, because the edge objects
     *              need to be constructed within the constructor in order to create the circular
     *              references back to the head nodes properly.
     */
    DAGNode(@NotNull V lbl, Iterable<Pair<DAGNode<V,E>,E>> edges) {
        label = lbl;
        ImmutableSet.Builder<DAGEdge<V,E>> bld = ImmutableSet.builder();
        for (Pair<DAGNode<V,E>,E> pair: edges) {
            DAGEdge<V,E> edge = new DAGEdge<V, E>(this, pair.getLeft(), pair.getRight());
            bld.add(edge);
        }
        outgoingEdges = bld.build();
        initializeCaches();
    }

    /**
     * Initialize caches for traversing this node.
     */
    private void initializeCaches() {
        reverseEdgeCache = Suppliers.memoize(new EdgeMapSupplier());
        reachableNodeCache = Suppliers.memoize(new NodeSetSupplier());
        topologicalSortCache = Suppliers.memoize(new TopologicalSortSupplier());
    }

    /**
     * Override the object reading protocol to make sure caches are instantiated.  This just calls
     * {@link #initializeCaches()} after doing the default object-reading.
     *
     * @param stream The stream to read from.
     * @throws IOException If an I/O exception occurs deserializing the object.
     * @throws ClassNotFoundException If there is a missing class deserializing the object.
     */
    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        initializeCaches();
    }

    /**
     * Get the label for this node.
     * @return The node's label.
     */
    @NotNull
    public V getLabel() {
        return label;
    }

    /**
     * Get the outgoing edges of this node.
     * @return The outgoing edges of the node.
     */
    @NotNull
    public Set<DAGEdge<V,E>> getOutgoingEdges() {
        return outgoingEdges;
    }

    /**
     * Get the outgoing edge with the specified target and label, if it exists.
     * @param target The target node.
     * @param label The label.
     * @return The edge from this node to {@code target} with label {@code label}, if it exists, or
     * {@code null} if no such edge exists.
     */
    public DAGEdge<V,E> getOutgoingEdge(DAGNode<V,E> target, E label) {
        for (DAGEdge<V,E> edge: outgoingEdges) {
            if (edge.getTail().equals(target) && edge.getLabel().equals(label)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Get an outgoing edge from this node with the specified label, if it exists.
     *
     * @param label The label.
     * @return An outgoing edge with the specified label, or {@code null} if no such edge exists.
     * If multiple edges have this label, an arbitrary one is returned.
     */
    public DAGEdge<V,E> getOutgoingEdgeWithLabel(E label) {
        return getOutgoingEdgeWithLabel(Predicates.equalTo(label));
    }

    /**
     * Search for an outgoing edge by a predicate.
     *
     * @param predicate A predicate over labels.
     * @return An outgoing edge matching the predicate, or {@code null} if no such edge exists.  If
     *         multiple edges have labels matching the predicate, it is undefined which one will be
     *         added.
     */
    public DAGEdge<V, E> getOutgoingEdgeWithLabel(Predicate<? super E> predicate) {
        Predicate<DAGEdge<?, E>> edgePred = DAGEdge.labelMatches(predicate);
        return Iterables.find(outgoingEdges, edgePred, null);
    }

    /**
     * Get the nodes that are adjacent to this node (only considering outgoing edges).
     * @return The set of adjacent nodes.
     */
    public Set<DAGNode<V,E>> getAdjacentNodes() {
        return FluentIterable.from(outgoingEdges)
                             .transform(DAGEdge.<V, E>extractTail())
                             .toSet();
    }

    /**
     * Get a multimap of incoming edges.  For each node reachable from this node, the map will
     * contain each of its incoming edges (also reachable from this graph).
     *
     * @return The reverse edge map.
     */
    @NotNull
    private SetMultimap<DAGNode<V,E>,DAGEdge<V,E>> getIncomingEdgeMap() {
        return reverseEdgeCache.get();
    }

    @NotNull
    public Set<DAGNode<V,E>> getReachableNodes() {
        return reachableNodeCache.get();
    }

    /**
     * Topographical sort all nodes reachable from the given root node. Nodes
     * that are farther away, or more connected, are at the beginning of the
     * list.
     * <p>
     * Nodes in the graph that are not connected to the root will not appear in
     * the returned list.
     * @return The sorted list of reachable nodes.
     */
    @NotNull
    public List<DAGNode<V,E>> getSortedNodes() {
        return topologicalSortCache.get();
    }

    /**
     * Helper mode for {@link #getSortedNodes()}, via {@link TopologicalSortSupplier}.  This method
     * does a depth-first traversal of the nodes, adding each to the {@code visited} set when it is
     * left.  This results in {@code visited} being a topological sort.
     *
     * @param visited The set of nodes seen so far.
     */
    private void sortVisit(LinkedHashSet<DAGNode<V,E>> visited) {
        if (!visited.contains(this)) {
            for (DAGEdge<V,E> nbr: outgoingEdges) {
                nbr.getTail().sortVisit(visited);
            }
            // neighbors won't have added this, or we have an impossible cycle
            assert !visited.contains(this);
            visited.add(this);
        }
    }

    /**
     * Get the incoming edges to a node reachable from this node.
     * @return The set of incoming edges, or an empty set if the node is not reachable.
     */
    @NotNull
    public Set<DAGEdge<V,E>> getIncomingEdges(DAGNode<V,E> node) {
        return getIncomingEdgeMap().get(node);
    }

    /**
     * Replace one node with another in this graph.  All edges referencing {@code node} are replaced
     * with edges referencing {@code replacement}.
     *
     * @param node The node to replace.
     * @param replacement The replacement node.
     * @param memory A table to remember node replacements.  It maintains a mapping of every node
     *               that has to be replaced with the node that replaces it.  This map should
     *               usually be empty on the initial call to this method.  In particular, it should
     *               not contain any reachable nodes on the initial call, or unexpected behavior
     *               may arise.  Recursive calls of this method to itself do contain such nodes.
     * @return The graph with the replaced node.
     */
    public DAGNode<V,E> replaceNode(DAGNode<V,E> node, DAGNode<V,E> replacement,
                                    Map<DAGNode<V,E>,DAGNode<V,E>> memory) {
        if (this.equals(node)) {
            memory.put(node, replacement);
            return replacement;
        } else if (memory.containsKey(this)) {
            // we have already been replaced, reuse the replacement
            return memory.get(this);
        } else if (getReachableNodes().contains(node)) {
            DAGNodeBuilder<V,E> bld = newBuilder(label);
            for (DAGEdge<V,E> edge: outgoingEdges) {
                DAGNode<V,E> newTail = edge.getTail().replaceNode(node, replacement, memory);
                bld.addEdge(newTail, edge.getLabel());
            }
            DAGNode<V,E> repl = bld.build();
            memory.put(this, repl);
            return repl;
        } else {
            return this;
        }
    }

    /**
     * Do a breadth-first search for a node.
     *
     * @param pred The predicate for matching nodes.
     * @return The first node matching {@code pred} in a breadth-first search, or {@code null} if no
     *         such node is found.
     */
    public DAGNode<V, E> findNodeBFS(@NotNull Predicate<? super DAGNode<V, E>> pred) {
        if (pred.apply(this)) {
            return this;
        }

        Queue<DAGNode<V, E>> work = Lists.newLinkedList();
        Set<DAGNode<V, E>> seen = Sets.newHashSet();
        work.add(this);
        seen.add(this);
        while (!work.isEmpty()) {
            DAGNode<V, E> node = work.remove();
            for (DAGEdge<V, E> e : node.getOutgoingEdges()) {
                // is this the node we are looking for?
                DAGNode<V, E> nbr = e.getTail();
                if (!seen.contains(nbr)) {
                    if (pred.apply(nbr)) {
                        return nbr;
                    } else {
                        seen.add(nbr);
                        work.add(nbr);
                    }
                }
            }
        }

        // no node found
        return null;
    }

    /**
     * Do a breadth-first search for an edge.
     *
     * @param pred The predicate for matching nodes.
     * @return The first node matching {@code pred} in a breadth-first search, or {@code null} if no
     *         such node is found.
     */
    public DAGEdge<V, E> findEdgeBFS(@NotNull Predicate<? super DAGEdge<V, E>> pred) {
        Queue<DAGNode<V, E>> work = Lists.newLinkedList();
        Set<DAGNode<V, E>> seen = Sets.newHashSet();
        work.add(this);
        seen.add(this);
        while (!work.isEmpty()) {
            DAGNode<V, E> node = work.remove();
            for (DAGEdge<V, E> e : node.getOutgoingEdges()) {
                // is this the edge we are looking for?
                if (pred.apply(e)) {
                    return e;
                } else if (!seen.contains(e.getTail())) {
                    seen.add(e.getTail());
                    work.add(e.getTail());
                }
            }
        }

        // no node found
        return null;
    }

    /**
     * Transform the edges in this graph.  Edges in parent nodes are passed <em>after</em> their
     * target nodes are rewritten, if necessary.
     *
     * @param function The edge transformation function.  Any edge returned by this function must
     *                 have the same head node as the function it was passed.  The transform
     *                 function may need to replace the head node on a returned edge; the label and
     *                 tail will be preserved.  If the function returns {@code null}, that is the
     *                 same as returning its input unmodified.
     * @return The rewritten graph.
     */
    public DAGNode<V,E> transformEdges(Function<? super DAGEdge<V,E>, ? extends DAGEdge<V,E>> function) {
        // builder for new node
        DAGNodeBuilder<V,E> builder = null;
        // intact edges (unmodified edges)
        List<DAGEdge<V,E>> intact = Lists.newArrayListWithCapacity(outgoingEdges.size());
        for (DAGEdge<V,E> edge: outgoingEdges) {
            DAGNode<V,E> tail = edge.getTail();
            DAGNode<V,E> transformedTail = tail.transformEdges(function);
            DAGEdge<V,E> toQuery = edge;
            if (transformedTail != tail) {
                // the node changed, query with the updated edge
                toQuery = DAGEdge.create(this, transformedTail, edge.getLabel());
            }
            DAGEdge<V,E> transformedEdge = function.apply(toQuery);
            if (transformedEdge == null) {
                transformedEdge = toQuery;
            }
            if (edge.equals(transformedEdge)) {
                // edge unmodified
                if (builder == null) {
                    intact.add(transformedEdge);
                } else {
                    builder.addEdge(transformedEdge.getTail(), transformedEdge.getLabel());
                }
            } else {
                // modified, need to transform this node
                if (builder == null) {
                    builder = newBuilder(label);
                    for (DAGEdge<V,E> done: intact) {
                        builder.addEdge(done.getTail(), done.getLabel());
                    }
                }
                builder.addEdge(transformedEdge.getTail(), transformedEdge.getLabel());
            }
        }

        if (builder != null) {
            return builder.build();
        } else {
            return this;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("node ")
          .append(label)
          .append(" with ")
          .append(getReachableNodes().size())
          .append(" nodes and ")
          .append(outgoingEdges.size())
          .append(" edges");
        return sb.toString();
    }

    public static <V> Predicate<DAGNode<V,?>> labelMatches(final Predicate<? super V> pred) {
        return new Predicate<DAGNode<V, ?>>() {
            @Override
            public boolean apply(@Nullable DAGNode<V, ?> input) {
                V lbl = input == null ? null : input.getLabel();
                return pred.apply(lbl);
            }
        };
    }

    /**
     * Supplier to compute the map of incoming edges.  Used to implement {@link #getIncomingEdgeMap()}.
     */
    private class EdgeMapSupplier implements Supplier<SetMultimap<DAGNode<V, E>, DAGEdge<V, E>>> {
        @Override
        public SetMultimap<DAGNode<V, E>, DAGEdge<V, E>> get() {
            ImmutableSetMultimap.Builder<DAGNode<V,E>,DAGEdge<V,E>> bld = ImmutableSetMultimap.builder();
            for (DAGEdge<V,E> nbr: outgoingEdges) {
                bld.put(nbr.getTail(), nbr);
                bld.putAll(nbr.getTail().getIncomingEdgeMap());
            }
            return bld.build();
        }
    }

    /**
     * Supplier to compute the set of reachable nodes.
     */
    private class NodeSetSupplier implements Supplier<Set<DAGNode<V, E>>> {
        @Override
        public ImmutableSet<DAGNode<V, E>> get() {
            return ImmutableSet.copyOf(getSortedNodes());
        }
    }

    private class TopologicalSortSupplier implements Supplier<List<DAGNode<V,E>>> {
        @Override
        public List<DAGNode<V, E>> get() {
            LinkedHashSet<DAGNode<V,E>> visited = Sets.newLinkedHashSet();
            sortVisit(visited);
            return ImmutableList.copyOf(visited);
        }
    }
}
