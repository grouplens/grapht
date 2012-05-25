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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grouplens.grapht.InjectorConfiguration;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
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
    private final InjectorConfiguration bindRules;
    
    private final Graph<Satisfaction, Desire> graph;
    private final Node<Satisfaction> root; // this has a null label

    /**
     * Create a DependencySolver that uses the given configuration, and max
     * depth of the dependency graph.
     * 
     * @param bindRules The bind rule configuration
     * @param maxDepth A maximum depth of the graph before it's determined that
     *            a cycle exists
     * @throws IllegalArgumentException if maxDepth is less than 1
     * @throws NullPointerException if bindRules is null
     */
    public DependencySolver(InjectorConfiguration bindRules, int maxDepth) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("Max depth must be at least 1");
        }
        if (bindRules == null) {
            throw new NullPointerException("InjectorConfiguration cannot be null");
        }
        
        this.bindRules = bindRules;
        this.maxDepth = maxDepth;
        
        graph = new Graph<Satisfaction, Desire>();
        root = new Node<Satisfaction>(null);
        graph.addNode(root);

        logger.info("DependencySolver created, max depth: {}", maxDepth);
    }
    
    /**
     * @return The InjectSPI used by this solver's configuration
     */
    public InjectSPI getSPI() {
        return bindRules.getSPI();
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

            resolveFully(desire, treeRoot, tree, new ArrayList<Pair<Satisfaction, Attributes>>());
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
                              List<Pair<Satisfaction, Attributes>> context) throws SolverException {
        // check context depth against max to detect likely dependency cycles
        if (context.size() > maxDepth) {
            throw new CyclicDependencyException(desire, "Maximum context depth of " + maxDepth + " was reached");
        }
        
        // resolve the current node
        Pair<Satisfaction, List<Desire>> resolved = resolve(desire, context);
        Node<Satisfaction> newNode = new Node<Satisfaction>(resolved.getLeft());
        
        // add the node to the graph, and connect it with its parent
        graph.addNode(newNode);
        graph.addEdge(new Edge<Satisfaction, List<Desire>>(parent, newNode, resolved.getRight()));
        
        // update the context
        List<Pair<Satisfaction, Attributes>> newContext = new ArrayList<Pair<Satisfaction, Attributes>>(context);
        newContext.add(Pair.of(resolved.getLeft(), desire.getAttributes()));
        
        List<? extends Desire> dependencies = resolved.getLeft().getDependencies();
        for (Desire d: dependencies) {
            // complete the sub graph for the given desire
            // - the call to resolveFully() is responsible for adding the dependency edges
            //   so we don't need to process the returned node
            logger.debug("Attempting to satisfy dependency: {}", d);
            resolveFully(d, newNode, graph, newContext);
        }
    }
    
    private Pair<Satisfaction, List<Desire>> resolve(Desire desire, List<Pair<Satisfaction, Attributes>> context) throws SolverException {
        // bind rules can only be used once when satisfying a desire,
        // this set will record all used bind rules so they are no longer considered
        Set<BindRule> appliedRules = new HashSet<BindRule>();
        
        List<Desire> desireChain = new ArrayList<Desire>();
        Desire currentDesire = desire;
        while(true) {
            logger.debug("Current desire: {}", currentDesire);
            // remember the current desire in the chain of followed desires
            desireChain.add(currentDesire);
            
            // collect all bind rules that apply to this desire
            List<Pair<ContextChain, BindRule>> validRules = new ArrayList<Pair<ContextChain, BindRule>>();
            for (ContextChain chain: bindRules.getBindRules().keySet()) {
                if (chain.matches(context)) {
                    // the context applies to the current context, so go through all
                    // bind rules within it and record those that match the desire
                    for (BindRule br: bindRules.getBindRules().get(chain)) {
                        if (br.matches(currentDesire) && !appliedRules.contains(br)) {
                            validRules.add(Pair.of(chain, br));
                            logger.trace("Matching rule, context: {}, rule: {}", chain, br);
                        }
                    }
                }
            }
            
            if (!validRules.isEmpty()) {
                // we have a bind rule to apply
                Comparator<Pair<ContextChain, BindRule>> ordering = Ordering.from(new ContextClosenessComparator(context))
                                                                            .compound(new ContextLengthComparator())
                                                                            .compound(new TypeDeltaComparator(context))
                                                                            .compound(new BindRuleComparator(currentDesire));
                Collections.sort(validRules, ordering);

                if (validRules.size() > 1) {
                    // must check if other rules are equal to the first
                    List<BindRule> topRules = new ArrayList<BindRule>();
                    topRules.add(validRules.get(0).getRight());
                    for (int i = 1; i < validRules.size(); i++) {
                        if (ordering.compare(validRules.get(0), validRules.get(i)) == 0) {
                            topRules.add(validRules.get(i).getRight());
                        }
                    }
                    
                    if (topRules.size() > 1) {
                        // additional rules match just as well as the first, so fail
                        throw new MultipleBindingsException(context, desireChain, topRules);
                    }
                }

                // apply the bind rule to get a new desire
                BindRule selectedRule = validRules.get(0).getRight();
                appliedRules.add(selectedRule);
                
                logger.debug("Applying rule: {} to desire: {}", selectedRule, currentDesire);
                currentDesire = selectedRule.apply(currentDesire);
                
                // possibly terminate if the bind rule terminates
                if (selectedRule.terminatesChain() && currentDesire.isInstantiable()) {
                    logger.info("Satisfied {} with {}", desire, currentDesire.getSatisfaction());
                    desireChain.add(currentDesire);
                    return Pair.of(currentDesire.getSatisfaction(), desireChain);
                }
            } else {
                // attempt to use the default desire, or terminate if we've found
                // a satisfiable desire
                Desire defaultDesire = currentDesire.getDefaultDesire();
                if (defaultDesire == null || desireChain.contains(defaultDesire)) {
                    // we don't use the default if there wasn't one, or using the
                    // default would create a cycle of desires
                    if (currentDesire.isInstantiable()) {
                        // the desire can be converted to a node, so we're done
                        logger.info("Satisfied {} with {}", desire, currentDesire.getSatisfaction());
                        return Pair.of(currentDesire.getSatisfaction(), desireChain);
                    } else {
                        // no more rules and we can't make a node
                        throw new UnresolvableDependencyException(context, desireChain);
                    }
                } else {
                    // continue with the default desire
                    logger.debug("Falling back to default desire: {}" + defaultDesire);
                    currentDesire = defaultDesire;
                }
            }
        }
    }

    /*
     * A Comparator that orders Pair<ContextChain, BindRule> based on the BindRule/Desire
     * implementation that orders BindRules.
     */
    private static class BindRuleComparator implements Comparator<Pair<ContextChain, BindRule>> {
        private final Desire desire;
        
        public BindRuleComparator(Desire desire) {
            this.desire = desire;
        }
        
        @Override
        public int compare(Pair<ContextChain, BindRule> o1, Pair<ContextChain, BindRule> o2) {
            Comparator<BindRule> ruleComparator = desire.ruleComparator();
            return ruleComparator.compare(o1.getRight(), o2.getRight());
        }
    }
    
    /*
     * A Comparator that compares rules based on the "type" delta of the matchers
     * with the nodes in the current context.
     * 
     * This comparator assumes that both context chains are the same length,
     * and that they match the exact same nodes in the context.
     */
    private static class TypeDeltaComparator implements Comparator<Pair<ContextChain, BindRule>> {
        private final List<Pair<Satisfaction, Attributes>> context;
        
        public TypeDeltaComparator(List<Pair<Satisfaction, Attributes>> context) {
            this.context = context;
        }
        
        @Override
        public int compare(Pair<ContextChain, BindRule> o1, Pair<ContextChain, BindRule> o2) {
            int lastIndex1 = o1.getLeft().getContexts().size() - 1;
            int lastIndex2 = o2.getLeft().getContexts().size() - 1;
            
            int matcher = 0; // measured from the last index
            for (int i = context.size() - 1; i >= 0; i--) {
                if (matcher > lastIndex1 || matcher > lastIndex2) {
                    // we've reached the end of one of the matcher chains
                    break;
                }
                
                Pair<Satisfaction, Attributes> currentNode = context.get(i);
                ContextMatcher m1 = o1.getLeft().getContexts().get(lastIndex1 - matcher);
                ContextMatcher m2 = o2.getLeft().getContexts().get(lastIndex2 - matcher);
                
                boolean match1 = m1.matches(currentNode);
                boolean match2 = m2.matches(currentNode);
                
                // if the chains match the same nodes, they should both match
                // or neither match
                assert match1 == match2;
                
                if (match1 && match2) {
                    // the chains apply to this node so we need to compare them
                    int cmp = currentNode.getLeft().contextComparator().compare(m1, m2);
                    if (cmp != 0) {
                        // one chain finally has a type delta difference, so the
                        // comparison of the chain equals the matcher comparison
                        return cmp;
                    } else {
                        // otherwise the matchers are equal so move to the next matcher
                        matcher++;
                    }
                }
            }
            
            // if we've gotten here, all matchers in each chain have the
            // same type delta to their respective matching nodes
            return 0;
        }
    }
    
    /*
     * A Comparator that compares rules based on how long the matching contexts are.
     */
    private static class ContextLengthComparator implements Comparator<Pair<ContextChain, BindRule>> {
        @Override
        public int compare(Pair<ContextChain, BindRule> o1, Pair<ContextChain, BindRule> o2) {
            int l1 = o1.getLeft().getContexts().size();
            int l2 = o2.getLeft().getContexts().size();
            // select longer contexts over shorter (i.e. longer < shorter)
            return l2 - l1;
        }
    }
    
    /*
     * A Comparator that compares rules based on how close a context matcher chain is to the
     * end of the current context.
     */
    private static class ContextClosenessComparator implements Comparator<Pair<ContextChain, BindRule>> {
        private final List<Pair<Satisfaction, Attributes>> context;
        
        public ContextClosenessComparator(List<Pair<Satisfaction, Attributes>> context) {
            this.context = context;
        }
        
        @Override
        public int compare(Pair<ContextChain, BindRule> o1, Pair<ContextChain, BindRule> o2) {
            int lastIndex1 = o1.getLeft().getContexts().size() - 1;
            int lastIndex2 = o2.getLeft().getContexts().size() - 1;
            
            int matcher = 0; // measured from the last index
            for (int i = context.size() - 1; i >= 0; i--) {
                if (matcher > lastIndex1 || matcher > lastIndex2) {
                    // we've reached the end of one of the matcher chains
                    break;
                }
                
                boolean match1 = o1.getLeft().getContexts().get(lastIndex1 - matcher).matches(context.get(i));
                boolean match2 = o2.getLeft().getContexts().get(lastIndex2 - matcher).matches(context.get(i));
                
                if (match1 && match2) {
                    // both chains match this context element, so go to the next matcher
                    matcher++;
                } else if (match1 && !match2) {
                    // first chain is closest match
                    return -1;
                } else if (!match1 && match2) {
                    // second chain is the closest match
                    return 1;
                } // else not matched in the context yet, so move up the context
            }
            
            // if we've made it here, both chains were equal up to the shortest chain,
            // or at least one of the chains was empty (that part is a little strange,
            // but we'll get correct results when we sort by context chain length next).
            return 0;
        }
    }
    
    private static class Ordering<T> implements Comparator<T> {
        private final List<Comparator<T>> comparators;
        
        private Ordering() {
            comparators = new ArrayList<Comparator<T>>();
        }
        
        public static <T> Ordering<T> from(Comparator<T> c) {
            if (c == null) {
                throw new NullPointerException("Comparator cannot be null");
            }
            Ordering<T> o = new Ordering<T>();
            o.comparators.add(c);
            return o;
        }
        
        public Ordering<T> compound(Comparator<T> c) {
            if (c == null) {
                throw new NullPointerException("Comparator cannot be null");
            }
            Ordering<T> no = new Ordering<T>();
            no.comparators.addAll(comparators);
            no.comparators.add(c);
            return no;
        }
        
        @Override
        public int compare(T o1, T o2) {
            for (Comparator<T> c: comparators) {
                int result = c.compare(o1, o2);
                if (result != 0) {
                    return result;
                }
            }
            return 0;
        }
    }
}
