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
package org.grouplens.grapht.solver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * FIXME Update the docs to match BindingFunctions
 * DependencySolver is a utility for resolving Desires into a dependency graph,
 * where nodes are shared when permitted by a Satisfaction's dependency
 * configuration. It supports qualified and context-aware injection, and
 * just-in-time injection if the type has an injectable constructor.
 * <p>
 * For more details on context management, see {@link ContextChain},
 * {@link ContextMatcher}, and {@link QualifierMatcher}. The DependencySolver
 * uses the context to activate and select BindRules. A number of rules are used
 * to order applicable BindRules and choose the best. When any of these rules
 * rely on the current dependency context, the deepest node in the context has
 * the most influence. Put another way, if contexts were strings, they could be
 * ordered lexicographically from the right to the left.
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
 * {@link Satisfaction#contextComparator()}</li>
 * <li>Bind rule type delta - BindRules are lastly ordered by how well their
 * type matches a particular desire, as determined by
 * {@link Desire#ruleComparator()}.</li>
 * </ol>
 * <p>
 * A summary of these rules is that the best specified BindRule is applied,
 * where the context that the BindRule is activated in has more priority than
 * the type of the BindRule. If multiple rules tie for best, then the solver
 * fails with a checked exception.
 * <p>
 * This solver does not support cyclic dependencies because of the possibility
 * that a context later on might activate a bind rule that breaks the cycle. To
 * ensure termination, it has a maximum context depth that is configurable.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class DependencySolver {
    private static final Logger logger = LoggerFactory.getLogger(DependencySolver.class);
    
    private final int maxDepth;
    private final List<BindingFunction> functions;
    
    private final Graph<Satisfaction, Desire> graph;
    private final Node<Satisfaction> root; // this has a null label

    /**
     * Create a DependencySolver that uses the given configuration, and max
     * depth of the dependency graph.
     * 
     * @param bindFunctions The binding functions that control desire bindings
     * @param maxDepth A maximum depth of the graph before it's determined that
     *            a cycle exists
     * @throws IllegalArgumentException if maxDepth is less than 1
     * @throws NullPointerException if bindFunctions is null
     */
    public DependencySolver(List<BindingFunction> bindFunctions, int maxDepth) {
        Preconditions.notNull("bindFunctions", bindFunctions);
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("Max depth must be at least 1");
        }
        
        this.functions = bindFunctions;
        this.maxDepth = maxDepth;
        
        graph = new Graph<Satisfaction, Desire>();
        root = new Node<Satisfaction>(null);
        graph.addNode(root);

        logger.info("DependencySolver created, max depth: {}", maxDepth);
    }
    
    /**
     * @return The resolved dependency graph
     */
    public Graph<Satisfaction, Desire> getGraph() {
        return graph;
    }
    
    /**
     * @return The root node of the graph, with a null label
     */
    public Node<Satisfaction> getRootNode() {
        return root;
    }
    
    /**
     * Update the dependency graph to include the given desire. An edge from the
     * root node to the desire's resolved satisfaction will exist after this is
     * finished.
     * 
     * @param desire The desire to include in the graph
     */
    public void resolve(Desire desire) throws SolverException {
        logger.info("Resolving desire: {}", desire);
        
        try {
            Graph<Satisfaction, List<Desire>> tree = new Graph<Satisfaction, List<Desire>>();
            Node<Satisfaction> treeRoot = new Node<Satisfaction>(null); // set label to null to identify it
            tree.addNode(treeRoot);

            resolveFully(desire, treeRoot, tree, new InjectionContext());
            merge(tree, treeRoot);
        } catch(SolverException e) {
            logger.error("Error while resolving: {}", e.getMessage());
            throw e;
        } catch(RuntimeException e) {
            logger.error("Error while resolving: {}", e.getMessage());
            throw e;
        }
    }
    
    private void merge(Graph<Satisfaction, List<Desire>> fullTree, Node<Satisfaction> root) {
        List<Node<Satisfaction>> sorted = fullTree.sort(root);
        
        // Look up each node's dependencies in the merged graph, since we sorted
        // by reverse depth we can guarantee that dependencies have already
        // been merged
        Map<Node<Satisfaction>, Node<Satisfaction>> mergedMap = new HashMap<Node<Satisfaction>, Node<Satisfaction>>();
        for (Node<Satisfaction> toMerge: sorted) {
            if (toMerge == root) {
                // This is the synthetic root of the tree.
                // We replace the root node of the tree with the root in the merged graph.
                for (Edge<Satisfaction, List<Desire>> oldEdge: fullTree.getOutgoingEdges(root)) {
                    Desire label = oldEdge.getLabel().get(0);
                    Node<Satisfaction> newTail = mergedMap.get(oldEdge.getTail());
                    assert newTail != null; // like below, it must have been merged previously
                    
                    // there can be at most one edge with this label in the merged
                    // graph because this is at the root context, and there is no
                    // way to cause their configurations to diverge
                    if (graph.getOutgoingEdge(this.root, label) ==  null) {
                        // this desire is not in the merged graph
                        graph.addEdge(new Edge<Satisfaction, Desire>(this.root, newTail, label));
                    }
                }
            } else {
                // Get all previously seen dependency configurations for this satisfaction
                Map<Set<Node<Satisfaction>>, Node<Satisfaction>> dependencyOptions = getDependencyOptions(toMerge.getLabel());
                
                // Accumulate the set of dependencies for this node, filtering
                // them through the previous level map
                Set<Node<Satisfaction>> dependencies = new HashSet<Node<Satisfaction>>();
                for (Edge<Satisfaction, List<Desire>> dep: fullTree.getOutgoingEdges(toMerge)) {
                    // levelMap converts from the tree to the merged graph
                    Node<Satisfaction> filtered = mergedMap.get(dep.getTail());
                    assert filtered != null; // all dependencies should have been merged previously
                    dependencies.add(filtered);
                }
                
                Node<Satisfaction> newNode = dependencyOptions.get(dependencies);
                if (newNode == null) {
                    // this configuration for the satisfaction has not been seen before
                    // - add it to merged graph, and connect to its dependencies
                    logger.debug("Adding new node to merged graph for satisfaction: {}", toMerge.getLabel());
                    
                    newNode = new Node<Satisfaction>(toMerge.getLabel());
                    graph.addNode(newNode);
                    
                    for (Edge<Satisfaction, List<Desire>> dep: fullTree.getOutgoingEdges(toMerge)) {
                        // add the edge with the new head and the previously merged tail
                        // List<Desire> is downsized to the first Desire, too
                        Node<Satisfaction> filtered = mergedMap.get(dep.getTail());
                        graph.addEdge(new Edge<Satisfaction, Desire>(newNode, filtered, dep.getLabel().get(0)));
                    }
                } else {
                    logger.debug("Node already in merged graph for satisfaction: {}", toMerge.getLabel());
                }

                // update merge map so future nodes use this node as a dependency
                mergedMap.put(toMerge, newNode);
            }
        }
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
    
    private void resolveFully(Desire desire, Node<Satisfaction> parent, Graph<Satisfaction, List<Desire>> graph, 
                              InjectionContext context) throws SolverException {
        // check context depth against max to detect likely dependency cycles
        if (context.getTypePath().size() > maxDepth) {
            throw new CyclicDependencyException(desire, "Maximum context depth of " + maxDepth + " was reached");
        }
        
        // resolve the current node
        Pair<Satisfaction, List<Desire>> resolved = resolve(desire, context);
        Node<Satisfaction> newNode = new Node<Satisfaction>(resolved.getLeft());
        
        // add the node to the graph, and connect it with its parent
        graph.addNode(newNode);
        graph.addEdge(new Edge<Satisfaction, List<Desire>>(parent, newNode, resolved.getRight()));
        
        for (Desire d: resolved.getLeft().getDependencies()) {
            // complete the sub graph for the given desire
            // - the call to resolveFully() is responsible for adding the dependency edges
            //   so we don't need to process the returned node
            logger.debug("Attempting to satisfy dependency {} of {}", d, resolved.getLeft());
            resolveFully(d, newNode, graph, context.push(resolved.getLeft(), desire.getInjectionPoint().getAttributes()));
        }
    }
    
    private Pair<Satisfaction, List<Desire>> resolve(Desire desire, InjectionContext context) throws SolverException {
        Desire currentDesire = desire;
        while(true) {
            logger.debug("Current desire: {}", currentDesire);
            
            BindingResult binding = null;
            for (BindingFunction bf: functions) {
                binding = bf.bind(context, currentDesire);
                if (binding.getDesire() != null) {
                    // found a binding, so don't continue to the next function
                    break;
                }
            }
            
            if (binding == null || binding.getDesire() == null) {
                if (currentDesire.isInstantiable()) {
                    logger.info("Satisfied {} with {}", desire, currentDesire.getSatisfaction());
                    // push current desire so that its included in the list of resolved desires
                    return Pair.of(currentDesire.getSatisfaction(), context.push(currentDesire).getPriorDesires());
                } else {
                    // desire cannot be satisfied
                    throw new UnresolvableDependencyException(currentDesire, context);
                }
            }
            
            // FIXME: handle deferred binding results
            // FIXME: I can clean this up
            // FIXME: prevent cycles if an already visited desire appears again
            //  - is this the responsibility of the BindingFunction or not?
            
            // update the context
            context = context.push(currentDesire);
            if (binding.terminates() && binding.getDesire().isInstantiable()) {
                logger.info("Satisfied {} with {}", desire, binding.getDesire().getSatisfaction());
                // push newly found desire so that its included in the list of resolved desires
                return Pair.of(binding.getDesire().getSatisfaction(), context.push(binding.getDesire()).getPriorDesires());
            }
            
            currentDesire = binding.getDesire();
        }
    }
}
