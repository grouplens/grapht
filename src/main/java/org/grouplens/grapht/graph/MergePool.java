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

import com.google.common.base.Functions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Merges graphs to remove redundant nodes.  This takes graphs and merges them, pruning redundant
 * nodes within the graphs and between graphs previously merged.  It remembers graphs it has
 * previously seen to allow nodes to be reused across multiple graphs.
 *
 * @param <V> The vertex type of graphs to merge.
 * @param <E> The edge type of graphs to merge.
 * @since 0.7
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MergePool<V,E> {
    // TODO Allow arbitrary equivalence relations over graph nodes so this class is less specialized.
    private static final Logger logger = LoggerFactory.getLogger(MergePool.class);

    private final Set<DAGNode<V,E>> pool;

    private MergePool() {
        pool = Sets.newLinkedHashSet();
    }

    /**
     * Create a merge pool that checks node labels for equality.
     *
     * @param <V> The node label type.
     * @param <E> The edge label type.
     * @return A new merge pool.
     */
    public static <V,E> MergePool<V,E> create() {
        return new MergePool<V, E>();
    }

    /**
     * Merge and simplify a graph.  This will coalesce redundant nodes (equivalent labels and
     * outgoing edge destinations), and will prefer to use nodes from graphs seen previously.
     * This allows deduplication across multiple graphs.
     *
     * <p><strong>Noteo:</strong> edge labels are ignored for the purpose of merging.</p>
     *
     * @param graph The graph to simplify.
     * @return The new simplified, merged graph.
     */
    public DAGNode<V,E> merge(DAGNode<V, E> graph) {
        List<DAGNode<V, E>> sorted = graph.getSortedNodes();

        Map<Pair<V,Set<DAGNode<V,E>>>, DAGNode<V,E>> nodeTable = Maps.newHashMap();
        for (DAGNode<V, E> node: pool) {
            Pair<V, Set<DAGNode<V, E>>> key =
                    Pair.of(node.getLabel(), node.getAdjacentNodes());
            assert !nodeTable.containsKey(key);
            nodeTable.put(key, node);
        }

        // We want to map nodes to their previous merged versions
        Map<DAGNode<V,E>, DAGNode<V,E>> mergedMap = Maps.newHashMap();
        // Now start processing nodes
        for (DAGNode<V, E> toMerge: sorted) {
            V sat = toMerge.getLabel();
            // Resolve the merged neighbors of this node.  They have already been
            // merged, since we are going in topological order.
            Set<DAGNode<V, E>> neighbors =
                    FluentIterable.from(toMerge.getOutgoingEdges())
                                  .transform(DAGEdge.<V,E>extractTail())
                                  .transform(Functions.forMap(mergedMap))
                                  .toSet();

            // See if we have already created an equivalent to this node
            DAGNode<V, E> newNode = nodeTable.get(Pair.of(sat, neighbors));
            if (newNode == null) {
                // No, let's start building one
                DAGNodeBuilder<V,E> bld = DAGNode.newBuilder();

                boolean changed = false;
                bld.setLabel(sat);
                logger.debug("Adding new node to merged graph for satisfaction: {}", sat);

                for (DAGEdge<V, E> edge: toMerge.getOutgoingEdges()) {
                    // create a new edge with the merged tail and same label
                    DAGNode<V, E> filtered = mergedMap.get(edge.getTail());
                    bld.addEdge(filtered, edge.getLabel());
                    // have we made a change to this node?
                    changed |= !filtered.equals(edge.getTail());
                }

                if (changed) {
                    // one of the node's neighbors has been replaced with merged version
                    // so use the new node
                    newNode = bld.build();
                } else {
                    // no edges were changed, leave the node unmodified
                    newNode = toMerge;
                }
                nodeTable.put(Pair.of(sat, neighbors), newNode);
            } else {
                logger.debug("Node already in merged graph for satisfaction: {}", toMerge.getLabel());
            }

            // update merge map so future equivalent nodes get replaced with this one
            mergedMap.put(toMerge, newNode);
        }

        // now let's find our return value - what did we merge the graph root to?
        DAGNode<V, E> newRoot = mergedMap.get(graph);
        // remember all its nodes for future merge operations
        pool.addAll(newRoot.getReachableNodes());
        // and we're done
        return newRoot;
    }
}
