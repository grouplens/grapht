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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.inject.graph.BindRule;
import org.grouplens.inject.graph.Desire;
import org.grouplens.inject.graph.Edge;
import org.grouplens.inject.graph.Graph;
import org.grouplens.inject.graph.Node;
import org.grouplens.inject.graph.NodeRepository;
import org.grouplens.inject.graph.Role;

import com.google.common.collect.Ordering;

/**
 * <p>
 * DefaultResolver is the default Resolver implementation. When resolving the
 * dependency graph for a desire, a "context" is built which consists of an
 * ordering of the nodes and their roles which satisfy each dependency. For more
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
 * {@link Node#contextComparator(Role)}.</li>
 * <li>Bind rule type delta - BindRules are lastly ordered by how well their
 * type matches a particular desire, as determined by
 * {@link Desire#ruleComparator()}.</li>
 * </ol>
 * <p>
 * A summary of these rules is that the best specified BindRule is applied,
 * where the context that the BindRule is activated in has more priority than
 * the type of the BindRule.
 * </p>
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class DefaultResolver implements Resolver {
    private final NodeRepository repository;
    private final int maxDepth;

    /**
     * <p>
     * Create a new DefaultResolver that uses the given NodeRepository for a
     * source of default bindings. The created resolver will use a max
     * dependency depth of 100 to guess if there are cycles in the dependency
     * hierarchy.
     * <p>
     * The provided NodeRepository must be compatible with any Desires and
     * BindRules that are passed to {@link #resolve(Collection, Map)}.
     * 
     * @param repository The NodeRepository to use
     * @throws NullPointerException if repository is null
     */
    public DefaultResolver(NodeRepository repository) {
        this(repository, 100);
    }

    /**
     * <p>
     * Create a new DefaultResolver that uses the given NodeRepository for a
     * source of default bindings. <tt>maxDepth</tt> represents the maximum
     * depth of the dependency hierarchy before it is assume that there is a
     * cycle. This constructor can be used to increase this depth in the event
     * that configuration requires it, although for most purposes the default
     * 100 should be sufficient.
     * <p>
     * The provided NodeRepository must be compatible with any Desires and
     * BindRules that are passed to {@link #resolve(Collection, Map)}.
     * 
     * @param repository The NodeRepository to use
     * @param maxDepth The maximum depth of the dependency hierarchy
     * @throws NullPointerException if repository is null
     * @throws IllegalArgumentException if maxDepth is less than 1
     */
    public DefaultResolver(NodeRepository repository, int maxDepth) {
        if (repository == null)
            throw new NullPointerException("NodeRepository cannot be null");
        if (maxDepth <= 0)
            throw new IllegalArgumentException("Max depth must be at least 1");
        
        this.repository = repository;
        this.maxDepth = maxDepth;
    }

    @Override
    public Graph resolve(Collection<Desire> rootDesires,
                         Map<ContextChain, Collection<BindRule>> bindRules) {
        Graph graph = new Graph();
        for (Desire desire: rootDesires) {
            // resolve all root desires, each starting at the empty context
            resolveFully(desire, graph, bindRules, new ArrayList<NodeAndRole>());
        }
        return graph;
    }
    
    private Node resolveFully(Desire desire, Graph graph, Map<ContextChain, Collection<BindRule>> bindRules, List<NodeAndRole> context) {
        // check context depth against max to detect likely dependency cycles
        if (context.size() > maxDepth)
            throw new ResolverException("Dependencies reached max depth of " + maxDepth + ", there is likely a dependency cycle");
        
        // resolve the current node
        Node resolved = resolve(desire, bindRules, context);
        graph.addNode(resolved);
        
        // update the context
        List<NodeAndRole> newContext = new ArrayList<NodeAndRole>(context);
        newContext.add(new NodeAndRole(resolved, desire.getRole()));
        
        List<Desire> dependencies = resolved.getDependencies();
        for (Desire d: dependencies) {
            // complete the sub graph for the given desire, and then record the
            // edge for this desire.
            Node completedDependency = resolveFully(d, graph, bindRules, newContext);
            graph.addEdge(new Edge(resolved, completedDependency, d));
        }
        return resolved;
    }
    
    private Node resolve(Desire desire, Map<ContextChain, Collection<BindRule>> bindRules, List<NodeAndRole> context) {
        // bind rules can only be used once when satisfying a desire,
        // this set will record all used bind rules so they are no longer considered
        Set<BindRule> appliedRules = new HashSet<BindRule>();
        
        Desire currentDesire = desire;
        while(!currentDesire.isInstantiable()) {
            // collect all bind rules that apply to this desire
            List<ContextAndBindRule> validRules = new ArrayList<ContextAndBindRule>();
            for (ContextChain chain: bindRules.keySet()) {
                if (chain.matches(context)) {
                    // the context applies to the current context, so go through all
                    // bind rules within it and record those that match the desire
                    for (BindRule br: bindRules.get(chain)) {
                        if (br.matches(currentDesire) && !appliedRules.contains(br)) {
                            validRules.add(new ContextAndBindRule(chain, br));
                        }
                    }
                }
            }
            
            // also add the default bind rule if it can apply to the desire
            BindRule defaultRule = repository.defaultBindRule();
            if (defaultRule.matches(currentDesire)) {
                // we use the null context to distinguish this rule from the empty context
                validRules.add(new ContextAndBindRule(null, defaultRule));
            }
            
            if (validRules.isEmpty()) {
                // no more bind rules to apply
                throw new ResolverException("Unable to satisfy desire: " + currentDesire + ", root desire: " + desire);
            }
            
            Comparator<ContextAndBindRule> ordering = Ordering.from(new ContextClosenessComparator(context))
                                                              .compound(new ContextLengthComparator())
                                                              .compound(new TypeDeltaComparator(context))
                                                              .compound(new BindRuleComparator(currentDesire));
            Collections.sort(validRules, ordering);
            
            if (validRules.size() > 1) {
                // must check if the 2nd bind rule is equivalent in order to the first
                if (ordering.compare(validRules.get(0), validRules.get(1)) == 0) {
                    // TODO REVIEW: return more information in the message?
                    throw new ResolverException("Too many choices for desire: " + currentDesire);
                }
            }
            
            BindRule selectedRule = validRules.get(0).rule;
            appliedRules.add(selectedRule);
            currentDesire = selectedRule.apply(currentDesire);
        }
        
        // at this point we have followed bind rules until we've reached
        // an instantiable desire
        return currentDesire.getNode();
    }

    /*
     * A Comparator that orders ContextAndBindRule based on the BindRule/Desire
     * implementation that orders BindRules.
     */
    private static class BindRuleComparator implements Comparator<ContextAndBindRule> {
        private final Desire desire;
        
        public BindRuleComparator(Desire desire) {
            this.desire = desire;
        }
        
        @Override
        public int compare(ContextAndBindRule o1, ContextAndBindRule o2) {
            Comparator<BindRule> ruleComparator = desire.ruleComparator();
            return ruleComparator.compare(o1.rule, o2.rule);
        }
    }
    
    /*
     * A Comparator that compares rules based on the "type" delta of the matchers
     * with the nodes in the current context.
     * 
     * This comparator assumes that both context chains are the same length,
     * and that they match the exact same nodes in the context.
     */
    private static class TypeDeltaComparator implements Comparator<ContextAndBindRule> {
        private final List<NodeAndRole> context;
        
        public TypeDeltaComparator(List<NodeAndRole> context) {
            this.context = context;
        }
        
        @Override
        public int compare(ContextAndBindRule o1, ContextAndBindRule o2) {
            // special cases for null contexts
            if (o1.context == null) {
                return (o2.context == null ? 0 : 1); // move null contexts to end of the list
            } else if (o2.context == null) {
                return (o1.context == null ? 0 : -1); //    ""
            }
            
            int lastIndex1 = o1.context.getContexts().size() - 1;
            int lastIndex2 = o2.context.getContexts().size() - 1;
            
            int matcher = 0; // measured from the last index
            for (int i = context.size() - 1; i >= 0; i--) {
                if (matcher > lastIndex1 || matcher > lastIndex2) {
                    // we've reached the end of one of the matcher chains
                    break;
                }
                
                NodeAndRole currentNode = context.get(i);
                ContextMatcher m1 = o1.context.getContexts().get(lastIndex1 - matcher);
                ContextMatcher m2 = o2.context.getContexts().get(lastIndex2 - matcher);
                
                boolean match1 = m1.matches(currentNode);
                boolean match2 = m2.matches(currentNode);
                
                // if the chains match the same nodes, they should both match
                // or neither match
                assert match1 == match2;
                
                if (match1 && match2) {
                    // the chains apply to this node so we need to compare them
                    int cmp = currentNode.getNode().contextComparator(currentNode.getRole()).compare(m1, m2);
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
    private static class ContextLengthComparator implements Comparator<ContextAndBindRule> {
        @Override
        public int compare(ContextAndBindRule o1, ContextAndBindRule o2) {
            // special cases for null contexts
            if (o1.context == null) {
                return (o2.context == null ? 0 : 1); // move null contexts to end of the list
            } else if (o2.context == null) {
                return (o1.context == null ? 0 : -1); //    ""
            }
            
            int l1 = o1.context.getContexts().size();
            int l2 = o2.context.getContexts().size();
            // select longer contexts over shorter (i.e. longer < shorter)
            return l2 - l1;
        }
    }
    
    /*
     * A Comparator that compares rules based on how close a context matcher chain is to the
     * end of the current context.
     */
    private static class ContextClosenessComparator implements Comparator<ContextAndBindRule> {
        private final List<NodeAndRole> context;
        
        public ContextClosenessComparator(List<NodeAndRole> context) {
            this.context = context;
        }
        
        @Override
        public int compare(ContextAndBindRule o1, ContextAndBindRule o2) {
            // special cases for null contexts
            if (o1.context == null) {
                return (o2.context == null ? 0 : 1); // move null contexts to end of the list
            } else if (o2.context == null) {
                return (o1.context == null ? 0 : -1); //    ""
            }
            
            int lastIndex1 = o1.context.getContexts().size() - 1;
            int lastIndex2 = o2.context.getContexts().size() - 1;
            
            int matcher = 0; // measured from the last index
            for (int i = context.size() - 1; i >= 0; i--) {
                if (matcher > lastIndex1 || matcher > lastIndex2) {
                    // we've reached the end of one of the matcher chains
                    break;
                }
                
                boolean match1 = o1.context.getContexts().get(lastIndex1 - matcher).matches(context.get(i));
                boolean match2 = o2.context.getContexts().get(lastIndex2 - matcher).matches(context.get(i));
                
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
    
    /*
     * ContextAndBindRule represents a paired context and bind rule. The context
     * limits the bind rule's scope.
     */
    private static class ContextAndBindRule {
        final ContextChain context; // this is null if the BindRule was the default rule from the NodeRepository
        final BindRule rule;
        
        public ContextAndBindRule(@Nullable ContextChain context, BindRule rule) {
            this.context = context;
            this.rule = rule;
        }
    }
}
