package org.grouplens.grapht;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.solver.BindingFunction;
import org.grouplens.grapht.solver.BindingResult;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.solver.MultipleBindingsException;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BindingFunction that uses BindRules created by the fluent API to bind desires
 * to other desires or satisfactions.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class RuleBasedBindingFunction implements BindingFunction {
    private static final String APPLIED_RULES = "APPLIED_BIND_RULES";
    
    private static final Logger logger = LoggerFactory.getLogger(RuleBasedBindingFunction.class);
    
    private final InjectSPI spi;
    private final Map<ContextChain, Collection<BindRule>> rules;
    
    public RuleBasedBindingFunction(InjectSPI spi, Map<ContextChain, Collection<BindRule>> rules) {
        Preconditions.notNull("spi", spi);
        Preconditions.notNull("rules", rules);
        
        this.spi = spi;
        this.rules = new HashMap<ContextChain, Collection<BindRule>>(rules);
    }
    
    @Override
    public BindingResult bind(InjectionContext context, Desire desire) throws SolverException {
        Set<BindRule> appliedRules = context.getValue(APPLIED_RULES);
        if (appliedRules == null) {
            appliedRules = new HashSet<BindRule>();
            context.putValue(APPLIED_RULES, appliedRules);
        }
        
        // collect all bind rules that apply to this desire
        List<Pair<ContextChain, BindRule>> validRules = new ArrayList<Pair<ContextChain, BindRule>>();
        for (ContextChain chain: rules.keySet()) {
            if (chain.matches(context.getTypePath())) {
                // the context applies to the current context, so go through all
                // bind rules within it and record those that match the desire
                for (BindRule br: rules.get(chain)) {
                    if (br.matches(desire) && !appliedRules.contains(br)) {
                        validRules.add(Pair.of(chain, br));
                        logger.trace("Matching rule, context: {}, rule: {}", chain, br);
                    }
                }
            }
        }
        
        if (!validRules.isEmpty()) {
            // we have a bind rule to apply
            Comparator<Pair<ContextChain, BindRule>> ordering = Ordering.from(new ContextClosenessComparator(context.getTypePath()))
                                                                        .compound(new ContextLengthComparator())
                                                                        .compound(new TypeDeltaComparator(context.getTypePath()))
                                                                        .compound(new BindRuleComparator());
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
                    throw new MultipleBindingsException(desire, context, topRules);
                }
            }

            // apply the bind rule to get a new desire
            BindRule selectedRule = validRules.get(0).getRight();
            appliedRules.add(selectedRule);
            
            logger.debug("Applying rule: {} to desire: {}", selectedRule, desire);
            return new BindingResult(selectedRule.apply(desire, spi), false, selectedRule.terminatesChain());
        }
        
        // No rule to apply, so return null to delegate to the next binding function
        return new BindingResult(null, false, false);
    }
    
    /*
     * A Comparator that orders Pair<ContextChain, BindRule> based on the
     * BindRule's qualifier matcher ordering
     */
    private static class BindRuleComparator implements Comparator<Pair<ContextChain, BindRule>> {
        @Override
        public int compare(Pair<ContextChain, BindRule> o1, Pair<ContextChain, BindRule> o2) {
            return o1.getRight().getQualifier().compareTo(o2.getRight().getQualifier());
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
