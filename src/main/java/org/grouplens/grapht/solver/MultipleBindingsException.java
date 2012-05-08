package org.grouplens.grapht.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Pair;

/**
 * Thrown when a desire has too many BindRules that match and there is no
 * single best rule to apply from the set of matched rules.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MultipleBindingsException extends ResolverException {
    private static final long serialVersionUID = 1L;

    private final List<Pair<Satisfaction, Qualifier>> context;
    private final List<Desire> desireChain;
    private final List<BindRule> bindRules;
    
    public MultipleBindingsException(List<Pair<Satisfaction, Qualifier>> context, List<Desire> desireChain, List<BindRule> bindRules) {
        this.context = Collections.unmodifiableList(new ArrayList<Pair<Satisfaction, Qualifier>>(context));
        this.desireChain = Collections.unmodifiableList(new ArrayList<Desire>(desireChain));
        this.bindRules = Collections.unmodifiableList(new ArrayList<BindRule>(bindRules));
    }
    
    /**
     * @return The context that produced the problematic bindings
     */
    public List<Pair<Satisfaction, Qualifier>> getContext() {
        return context;
    }
    
    /**
     * @return The top bind rules that could not be reduced to a single rule,
     *         the list will have at least 2 elements
     */
    public List<BindRule> getBindRules() {
        return bindRules;
    }
    
    /**
     * <p>
     * Get the list of desires that were being resolved. The first Desire in the
     * list represents the desire exposed by the last satisfaction in the
     * context. The last desire is the desire that matched too many bind rules.
     * <p>
     * Any desires between those two are intermediate desires that were the
     * result of applying other rules.
     * 
     * @return The desire chain that produced too many rules
     */
    public List<Desire> getDesires() {
        return desireChain;
    }
    
    @Override
    public String getMessage() {
        // header
        StringBuilder sb = new StringBuilder("Too many choices for desire: ")
            .append(desireChain.get(desireChain.size() - 1))
            .append('\n');
        
        // bind rules
        sb.append("Possible bindings:\n");
        for (BindRule rule: bindRules) {
            sb.append('\t').append(rule).append('\n');
        }
        sb.append('\n');
        
        // context
        sb.append("Current context:\n");
        for (Pair<Satisfaction, Qualifier> ctx: context) {
            sb.append('\t');
            if (ctx.getRight() != null) {
                sb.append(ctx.getRight()).append(':');
            }
            sb.append(ctx.getLeft()).append('\n');
        }
        sb.append('\n');
        
        // desire chain
        sb.append("Desire resolution:\n");
        for (Desire desire: desireChain) {
            sb.append('\t').append(desire).append('\n');
        }
        
        return sb.toString();
    }
}
