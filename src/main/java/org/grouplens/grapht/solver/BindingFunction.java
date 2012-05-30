package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.Desire;

public interface BindingFunction {
    BindingResult bind(InjectionContext context, Desire desire) throws SolverException;
}
