package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;

/**
 * Thrown by when a cyclic dependency is detected and could not be broken or
 * bypassed by the solver.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class CyclicDependencyException extends ResolverException {
    private static final long serialVersionUID = 1L;

    private final Satisfaction parent;
    private final Desire desire;
    
    public CyclicDependencyException(Satisfaction parent, Desire desire, String msg) {
        super(msg);
        this.parent = parent;
        this.desire = desire;
    }
    
    /**
     * @return The last resolved satisfaction before the cycle was detected
     */
    public Satisfaction getParentSatisfaction() {
        return parent;
    }
    
    /**
     * @return The current desire that triggered the cycle detection
     */
    public Desire getDesire() {
        return desire;
    }
    
    @Override
    public String getMessage() {
        // header
        StringBuilder sb = new StringBuilder("Cycle detected: ")
            .append(super.getMessage())
            .append('\n');
        
        // last satisfaction
        sb.append("Last resolved element:\n")
          .append('\t')
          .append(parent)
          .append('\n')
          .append('\n');
        
        // current desire
        sb.append("Unsatisfiable desire:\n")
          .append('\t')
          .append(desire)
          .append('\n');
        
        return sb.toString();
    }
}
