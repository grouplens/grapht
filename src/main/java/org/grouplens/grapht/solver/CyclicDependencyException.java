package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.Desire;

/**
 * Thrown by when a cyclic dependency is detected and could not be broken or
 * bypassed by the solver.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class CyclicDependencyException extends ResolverException {
    private static final long serialVersionUID = 1L;

    private final Desire desire;
    
    public CyclicDependencyException(Desire desire, String msg) {
        super(msg);
        this.desire = desire;
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
        StringBuilder sb = new StringBuilder(super.getMessage())
            .append('\n');
        
        // current desire
        sb.append("Unsatisfiable desire:\n")
          .append('\t')
          .append(formatDesire(desire))
          .append('\n');
        
        return sb.toString();
    }
}
