package org.grouplens.grapht.solver;

import org.grouplens.grapht.InjectionException;

/**
 * Deprecated alias for {@link org.grouplens.grapht.ResolutionException}.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
@Deprecated
public class SolverException extends InjectionException {
    public SolverException() {
        super();
    }

    public SolverException(String message) {
        super(message);
    }


    public SolverException(Throwable cause) {
        super(cause);
    }

    public SolverException(String message, Throwable cause) {
        super(message, cause);
    }
}
