package org.grouplens.grapht.solver;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;

public interface BindingFunction {
    Desire bind(List<Pair<Satisfaction, Attributes>> context, Desire desire) throws SolverException;
    
    // FIXME: not the best here
    boolean isDeferred(Desire d);
}
