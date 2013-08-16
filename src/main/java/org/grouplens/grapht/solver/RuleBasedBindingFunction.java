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

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.ContextMatch;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * BindingFunction that uses BindRules created by the fluent API to bind desires
 * to other desires or satisfactions.
 * <p>
 * For more details on context management, see {@link org.grouplens.grapht.spi.ElementChainContextMatcher},
 * {@link org.grouplens.grapht.spi.ContextElementMatcher}, and {@link QualifierMatcher}. This function uses the
 * context to activate and select BindRules. A number of rules are used to order
 * applicable BindRules and choose the best. When any of these rules rely on the
 * current dependency context, the deepest node in the context has the most
 * influence. Put another way, if contexts were strings, they could be ordered
 * lexicographically from the right to the left.
 * <p>
 * When selecting BindRules to apply to a Desire, BindRules are ordered first by
 * {@linkplain ContextMatch context match}, then by the ordering defined by the bind rule itself.
 * <p>
 * A summary of these rules is that the best specified BindRule is applied,
 * where the context that the BindRule is activated in has more priority than
 * the type of the BindRule. If multiple rules tie for best, then the solver
 * fails with a checked exception.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class RuleBasedBindingFunction implements BindingFunction {
    private static final String APPLIED_RULES = "APPLIED_BIND_RULES";
    
    private static final Logger logger = LoggerFactory.getLogger(RuleBasedBindingFunction.class);
    
    private final Map<ContextMatcher, Collection<BindRule>> rules;
    
    public RuleBasedBindingFunction(Map<ContextMatcher, Collection<BindRule>> rules) {
        Preconditions.notNull("rules", rules);
        
        this.rules = Collections.unmodifiableMap(new HashMap<ContextMatcher, Collection<BindRule>>(rules));
    }
    
    /**
     * @return The rules used by this BindingFunction
     */
    public Map<ContextMatcher, Collection<BindRule>> getRules() {
        return rules;
    }
    
    @Override
    public BindingResult bind(InjectionContext context, Desire desire) throws SolverException {
        Set<BindRule> appliedRules = context.getValue(APPLIED_RULES);
        if (appliedRules == null) {
            appliedRules = new HashSet<BindRule>();
            context.putValue(APPLIED_RULES, appliedRules);
        }
        
        // collect all bind rules that apply to this desire
        List<Pair<ContextMatch, BindRule>> validRules = new ArrayList<Pair<ContextMatch, BindRule>>();
        for (ContextMatcher matcher: rules.keySet()) {
            ContextMatch match = matcher.matches(context);
            if (match != null) {
                // the context applies to the current context, so go through all
                // bind rules within it and record those that match the desire
                for (BindRule br: rules.get(matcher)) {
                    if (br.matches(desire) && !appliedRules.contains(br)) {
                        validRules.add(Pair.of(match, br));
                        logger.trace("Matching rule, context: {}, rule: {}", matcher, br);
                    }
                }
            }
        }
        
        if (!validRules.isEmpty()) {
            // we have a bind rule to apply
            // pair's ordering is suitable for sorting the bind rules
            Collections.sort(validRules);

            if (validRules.size() > 1) {
                // must check if other rules are equal to the first
                // we find the whole list of dupes for error reporting purposes
                List<BindRule> topRules = new ArrayList<BindRule>();
                topRules.add(validRules.get(0).getRight());
                for (int i = 1; i < validRules.size(); i++) {
                    if (validRules.get(0).compareTo(validRules.get(i)) == 0) {
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
            return new BindingResult(selectedRule.apply(desire), selectedRule.getCachePolicy(),
                                     false, selectedRule.isTerminal());
        }
        
        // No rule to apply, so return null to delegate to the next binding function
        return null;
    }
}
