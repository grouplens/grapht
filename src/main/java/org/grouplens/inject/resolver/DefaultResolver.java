/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.resolver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.grouplens.inject.InjectorConfiguration;
import org.grouplens.inject.graph.Edge;
import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.graph.Node;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.spi.Satisfaction;

import com.google.common.base.Function;

/**
 * <p>
 * DefaultResolver is the default Resolver implementation. When resolving the
 * dependency graph for a desire, a "context" is built which consists of an
 * ordering of the nodes and their {@link Qualifier}s which satisfy each dependency. For more
 * details, see {@link ContextChain} and {@link ContextMatcher}. The
 * DefaultResolver uses the context to activate and select BindRules. A number
 * of rules are used to order applicable BindRules and choose the best. When any
 * of these rules rely on the current dependency context, the deepest node in
 * the context has the most influence. Put another way, if contexts were
 * strings, they could be ordered lexicographically from the right to the left.
 * <p>
 * When selecting BindRules to apply to a Desire, BindRules are ordered by the
 * following rules:
 * <ol>
 * <li>Context closeness - BindRules with a context matching chain closer to the
 * leaf nodes of the current dependency context are selected.</li>
 * <li>Context chain length - BindRules with a longer context chain are
 * selected.</li>
 * <li>Context chain type delta - BindRules are ordered by how close their
 * context matching chain is to the current dependency context, as determined by
 * {@link Node#contextComparator(Qualifier)}.</li>
 * <li>Bind rule type delta - BindRules are lastly ordered by how well their
 * type matches a particular desire, as determined by
 * {@link Desire#ruleComparator()}.</li>
 * </ol>
 * <p>
 * A summary of these rules is that the best specified BindRule is applied,
 * where the context that the BindRule is activated in has more priority than
 * the type of the BindRule.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class DefaultResolver implements Resolver {
    private final DependencyResolver resolver;
    private final Graph<Satisfaction, Desire> graph;
    
    private final Node<Satisfaction> root; // this has a null label
    
    private final Map<Node<Satisfaction>, Provider<?>> providerCache;

    /**
     * <p>
     * Create a new DefaultResolver. The created resolver will use a max
     * dependency depth of 100 to estimate if there are cycles in the dependency
     * hierarchy.
     */
    public DefaultResolver(InjectorConfiguration config) {
        this(config, 100);
    }

    /**
     * <p>
     * Create a new DefaultResolver. <tt>maxDepth</tt> represents the maximum
     * depth of the dependency hierarchy before it is assume that there is a
     * cycle. This constructor can be used to increase this depth in the event
     * that configuration requires it, although for most purposes the default
     * 100 should be sufficient.
     * 
     * @param maxDepth The maximum depth of the dependency hierarchy
     * @throws IllegalArgumentException if maxDepth is less than 1
     */
    public DefaultResolver(InjectorConfiguration config, int maxDepth) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("Max depth must be at least 1");
        }
        if (config == null) {
            throw new NullPointerException("InjectorConfiguration cannot be null");
        }
        
        resolver = new DependencyResolver(config.getBindRules(), maxDepth);
        graph = new Graph<Satisfaction, Desire>();
        root = new Node<Satisfaction>(null);
        providerCache = new HashMap<Node<Satisfaction>, Provider<?>>();
    }

    @Override
    public Provider<?> resolve(Desire desire) {
        // Look up an outgoing edge for this desire
        Edge<Satisfaction, Desire> resolved = graph.getOutgoingEdge(root, desire);
        
        // This is only non-null if the desire was requested before at the root context,
        // it may be present in the graph at a deeper node. If that's the case
        // it will be properly merged after regenerating the graph at the root context.
        if (resolved == null) {
            // Delegate to the dependency resolver to update our graph
            Graph<Satisfaction, List<Desire>> dependencyTree = resolver.resolve(desire);
            merge(dependencyTree);
            resolved = graph.getOutgoingEdge(root, desire);
        }
        
        // Check if the provider for the resolved node is in our cache
        Node<Satisfaction> resolvedNode = resolved.getTail();
        return getProvider(resolvedNode);
    }
    
    @Override
    public Graph<Satisfaction, Desire> getGraph() {
        return graph;
    }
    
    private void merge(Graph<Satisfaction, List<Desire>> fullTree) {
        // accumulate all leaf nodes for the initial merge set
        Set<Node<Satisfaction>> leafNodes = new HashSet<Node<Satisfaction>>();
        for (Node<Satisfaction> n: fullTree.getNodes()) {
            // no outgoing edges implies no dependencies so it's a leaf
            if (fullTree.getOutgoingEdges(n).isEmpty()) {
                leafNodes.add(n);
            }
        }
        
        // merge all nodes with equivalent configurations, 
        // it is safe to pass null in for the levelMap because the first iteration
        // is only leaf nodes (so they have no outgoing edges and will not need
        // to query the level map).
        deduplicate(leafNodes, null, fullTree);
    }
    
    private Map<Set<Node<Satisfaction>>, Node<Satisfaction>> getDependencyOptions(Satisfaction satisfaction) {
        // build a base map of dependency configurations to nodes for the provided
        // satisfaction, using the current state of the graph
        Map<Set<Node<Satisfaction>>, Node<Satisfaction>> options = new HashMap<Set<Node<Satisfaction>>, Node<Satisfaction>>();
        for (Node<Satisfaction> node: graph.getNodes()) {
            if (satisfaction.equals(node.getLabel())) {
                // accumulate all of its immediate dependencies
                Set<Node<Satisfaction>> option = new HashSet<Node<Satisfaction>>();
                for (Edge<Satisfaction, Desire> edge: graph.getOutgoingEdges(node)) {
                    option.add(edge.getTail());
                }
                options.put(option, node);
            }
        }
        return options;
    }
    
    private void deduplicate(Set<Node<Satisfaction>> toMerge, Map<Node<Satisfaction>, Node<Satisfaction>> levelMap, 
                              Graph<Satisfaction, List<Desire>> tree) {
        // check the termination condition - toMerge contains a single node with
        // a null satisfaction
        if (toMerge.size() == 1) {
            Node<Satisfaction> root = toMerge.iterator().next();
            if (root.getLabel() == null) {
                // we replace the root node of the tree with the root in the merged graph.
                for (Edge<Satisfaction, List<Desire>> oldEdge: tree.getOutgoingEdges(root)) {
                    Desire label = oldEdge.getLabel().get(0);
                    Node<Satisfaction> newTail = levelMap.get(oldEdge.getTail());
                    assert newTail != null; // like below, it must have been merged previously
                    
                    // there can be at most one edge with this label in the merged
                    // graph because this is at the root context, and there is no
                    // way to cause their configurations to diverge
                    if (graph.getOutgoingEdge(this.root, label) ==  null) {
                        // this desire is not in the merged graph
                        graph.addEdge(new Edge<Satisfaction, Desire>(this.root, newTail, label));
                    }
                }
                return;
            } // else it wasn't actually the root so keep going below
        } else {
            // if we don't have exactly 1 node left, there should be > 1,
            // otherwise the tree is in an inconsistent state
            assert !toMerge.isEmpty();
        }
        
        // map from nodes in toMerge to their potentially merged new nodes in the merged graph
        //  - key nodes are in tree, value nodes are in merged graph
        Map<Node<Satisfaction>, Node<Satisfaction>> currentLevelMap = new HashMap<Node<Satisfaction>, Node<Satisfaction>>();
        
        // accumulated set of all incoming edges to nodes in toMerge
        //  - nodes are in tree graph
        Set<Node<Satisfaction>> nextMerge = new HashSet<Node<Satisfaction>>();

        // this is a map from satisfactions to a map of its possible dependency
        // configurations to the new node in the merged graph
        //  - all nodes are in the merged graph
        Map<Satisfaction, Map<Set<Node<Satisfaction>>, Node<Satisfaction>>> mergeMap = new HashMap<Satisfaction, Map<Set<Node<Satisfaction>>,Node<Satisfaction>>>();
        
        for (Node<Satisfaction> oldNode: toMerge) {
            // lookup map of all node's dependency possibilities
            Map<Set<Node<Satisfaction>>, Node<Satisfaction>> depOptions = mergeMap.get(oldNode.getLabel());
            if (depOptions == null) {
                // get the dependency options based on the previously accumulated
                // graph state (this is responsible for integrating the new graph
                // into the old graph)
                depOptions = getDependencyOptions(oldNode.getLabel());
                mergeMap.put(oldNode.getLabel(), depOptions);
            }
            
            // accumulate the set of dependencies for this node, filtering
            // them through the previous level map
            Set<Node<Satisfaction>> dependencies = new HashSet<Node<Satisfaction>>();
            for (Edge<Satisfaction, List<Desire>> dep: tree.getOutgoingEdges(oldNode)) {
                // levelMap converts from the tree to the merged graph
                Node<Satisfaction> filtered = levelMap.get(dep.getTail());
                assert filtered != null; // all dependencies should have been merged previously
                dependencies.add(filtered);
            }
            
            Node<Satisfaction> newNode = depOptions.get(dependencies);
            if (newNode == null) {
                // this configuration for the satisfaction has not been seen before
                // - add it to merged graph, and connect to its dependencies
                newNode = new Node<Satisfaction>(oldNode.getLabel());
                graph.addNode(newNode);
                
                for (Edge<Satisfaction, List<Desire>> dep: tree.getOutgoingEdges(oldNode)) {
                    // add the edge with the new head and the previously merged tail
                    // List<Desire> is downsized to the first Desire, too
                    Node<Satisfaction> filtered = levelMap.get(dep.getTail());
                    graph.addEdge(new Edge<Satisfaction, Desire>(newNode, filtered, dep.getLabel().get(0)));
                }
                
                // update merge map so subsequent appearances of this configuration reuse this node,
                //  - since newNode is now in the merged graph, future calls to getDependencyOptions()
                //    will be able to rebuild this key-value pair
                depOptions.put(dependencies, newNode);
            }
            
            // we have the merged node, record it in the level map for the next iteration,
            // and accumulate all incoming nodes (which will have to be merged next)
            currentLevelMap.put(oldNode, newNode);
            for (Edge<Satisfaction, List<Desire>> parent: tree.getIncomingEdges(oldNode)) {
                nextMerge.add(parent.getHead());
            }
        }
        
        // recurse to the next level in the dependency hierarchy
        deduplicate(nextMerge, currentLevelMap, tree);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Provider<?> getProvider(Node<Satisfaction> node) {
        Provider<?> cached = providerCache.get(node);
        if (cached == null) {
            Provider<?> raw = node.getLabel().makeProvider(new DesireProviderMapper(node));
            cached = new MemoizingProvider(raw);
            providerCache.put(node, cached);
        }
        return cached;
    }
    
    private class MemoizingProvider<T> implements Provider<T> {
        private final Provider<T> raw;
        private T cached;
        
        public MemoizingProvider(Provider<T> raw) {
            this.raw = raw;
        }
        
        @Override
        public T get() {
            if (cached == null) {
                cached = raw.get();
            }
            return cached;
        }
    }
    
    private class DesireProviderMapper implements Function<Desire, Provider<?>> {
        private final Node<Satisfaction> forNode;
        
        public DesireProviderMapper(Node<Satisfaction> forNode) {
            this.forNode = forNode;
        }
        
        @Override
        public Provider<?> apply(Desire desire) {
            Edge<Satisfaction, Desire> edge = graph.getOutgoingEdge(forNode, desire);
            Node<Satisfaction> dependency = edge.getTail();
            return getProvider(dependency);
        }
    }
}
