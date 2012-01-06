/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.resolver;

import java.util.Map;

import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.graph.Node;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Satisfaction;

/**
 * ResolverResult contains the resolved dependency information that is computed
 * by a {@link Resolver}.
 * 
 * @author Michael Ludwig
 */
// TODO: Add the ability to query for warnings
// TODO: Add another graph that contains a list of desires along edges, and
//       the graph is not de-duplicated.
public class ResolverResult {
    private final Graph<Satisfaction, Desire> graph;
    private final Node<Satisfaction> rootNode;

    /**
     * Create a new ResolverResult that uses the given graph and mapping. The
     * mapping's state is copied so future changes to the Map will not affect
     * the ResolverResult.
     * 
     * @param graph The resolved dependency graph
     * @param rootNode The node containing the root satisfaction that was resolved
     * @throws NullPointerException if any argument is null
     */
    public ResolverResult(Graph<Satisfaction, Desire> graph, Node<Satisfaction> rootNode) {
        if (graph == null || rootNode == null)
            throw new NullPointerException("Graph and root node cannot be null");
        this.graph = graph;
        this.rootNode = rootNode;
    }

    /**
     * Get the graph containing the resolved dependencies from a call to
     * {@link Resolver#resolve(java.util.Collection, Map)}. The returned Graph
     * is mutable and this does not make a defensive copy.
     * 
     * @return The resolved dependency graph
     */
    public Graph<Satisfaction, Desire> getGraph() {
        return graph;
    }

    /**
     * Get the root node containing the input satisfaction to be resolved.
     * 
     * @return The root node wrapping the original root satisfaction
     */
    public Node<Satisfaction> getRootNode() {
        return rootNode;
    }
}
