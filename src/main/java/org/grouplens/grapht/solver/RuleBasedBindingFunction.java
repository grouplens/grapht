/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.solver;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.context.ContextMatch;
import org.grouplens.grapht.context.ContextMatcher;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>
 * BindingFunction that uses BindRules created by the fluent API to bind desires
 * to other desires or satisfactions.
 * <p>
 * For more details on context management, see {@link org.grouplens.grapht.context.ContextPattern},
 * {@link org.grouplens.grapht.context.ContextElementMatcher}, and {@link QualifierMatcher}. This function uses the
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
    private static final Map<Object,Set<BindRule>> bindRuleMemory
            = new WeakHashMap<Object, Set<BindRule>>();

    private static final Logger logger = LoggerFactory.getLogger(RuleBasedBindingFunction.class);
    
    private final ImmutableListMultimap<ContextMatcher, BindRule> rules;
    
    public RuleBasedBindingFunction(Multimap<ContextMatcher, BindRule> rules) {
        Preconditions.notNull("rules", rules);
        
        this.rules = ImmutableListMultimap.copyOf(rules);
    }
    
    /**
     * Get the rules underlying this binding function.
     * @return The rules used by this BindingFunction
     */
    public ListMultimap<ContextMatcher, BindRule> getRules() {
        return rules;
    }
    
    @Override
    public BindingResult bind(InjectionContext context, DesireChain desire) throws ResolutionException {
        // FIXME Build a better way to remember the applied rules
        Set<BindRule> appliedRules;
        synchronized (bindRuleMemory) {
            appliedRules = bindRuleMemory.get(desire.getKey());
            if (appliedRules == null) {
                appliedRules = new HashSet<BindRule>();
                bindRuleMemory.put(desire.getKey(), appliedRules);
            }
        }

        // collect all bind rules that apply to this desire
        List<Pair<ContextMatch, BindRule>> validRules = new ArrayList<Pair<ContextMatch, BindRule>>();
        for (ContextMatcher matcher: rules.keySet()) {
            ContextMatch match = matcher.matches(context);
            if (match != null) {
                // the context applies to the current context, so go through all
                // bind rules within it and record those that match the desire
                for (BindRule br: rules.get(matcher)) {
                    if (br.matches(desire.getCurrentDesire()) && !appliedRules.contains(br)) {
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
                    logger.error("{} bindings for {} in {}", topRules.size(),
                                 desire, context);
                    for (BindRule rule: topRules) {
                        logger.info("matching rule: {}", rule);
                    }
                    // additional rules match just as well as the first, so fail
                    throw new MultipleBindingsException(desire, context, topRules);
                }
            }

            // apply the bind rule to get a new desire
            BindRule selectedRule = validRules.get(0).getRight();
            appliedRules.add(selectedRule);
            
            logger.debug("Applying rule: {} to desire: {}", selectedRule, desire);
            return BindingResult.newBuilder()
                                .setDesire(selectedRule.apply(desire.getCurrentDesire()))
                                .setCachePolicy(selectedRule.getCachePolicy())
                                .setFlags(selectedRule.getFlags())
                                .build();
        }
        
        // No rule to apply, so return null to delegate to the next binding function
        return null;
    }
}
