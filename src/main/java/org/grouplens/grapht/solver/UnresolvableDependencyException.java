package org.grouplens.grapht.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Pair;

/**
 * Thrown when a desire cannot be resolved to an instantiable satisfaction.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class UnresolvableDependencyException extends ResolverException {
    private static final long serialVersionUID = 1L;

    private final List<Pair<Satisfaction, Qualifier>> context;
    private final List<Desire> desireChain;
    
    public UnresolvableDependencyException(List<Pair<Satisfaction, Qualifier>> context, List<Desire> desireChain) {
        this.context = Collections.unmodifiableList(new ArrayList<Pair<Satisfaction, Qualifier>>(context));
        this.desireChain = Collections.unmodifiableList(new ArrayList<Desire>(desireChain));
    }
    
    /**
     * @return The context that produced the unresolvable desire
     */
    public List<Pair<Satisfaction, Qualifier>> getContext() {
        return context;
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
        StringBuilder sb = new StringBuilder("Unable to satisfy desire: ")
            .append(formatDesire(desireChain.get(desireChain.size() - 1)))
            .append('\n');
        
        // context
        sb.append("Current context:\n");
        for (Pair<Satisfaction, Qualifier> ctx: context) {
            sb.append('\t').append(formatContext(ctx)).append('\n');
        }
        sb.append('\n');
        
        // desire chain
        sb.append("Desire resolution:\n");
        for (Desire desire: desireChain) {
            sb.append('\t').append(formatDesire(desire)).append('\n');
        }
        
        return sb.toString();
    }
}
