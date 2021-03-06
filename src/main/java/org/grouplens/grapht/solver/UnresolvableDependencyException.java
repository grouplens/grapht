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

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;

import java.util.List;

/**
 * Thrown when a desire cannot be resolved to an instantiable satisfaction.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class UnresolvableDependencyException extends ResolutionException {
    private static final long serialVersionUID = 1L;

    private final DesireChain desires;
    private final InjectionContext context;
    
    public UnresolvableDependencyException(DesireChain chain, InjectionContext context) {
        this.desires = chain;
        this.context = context;
    }

    public UnresolvableDependencyException(DesireChain chain, InjectionContext context, Throwable cause) {
        super(cause);
        this.desires = chain;
        this.context = context;
    }
    
    /**
     * Get the context for this error.
     *
     * @return The context that produced the unresolvable desire
     */
    public InjectionContext getContext() {
        return context;
    }

    /**
     * Get the entire desire chain in which resolution failed.
     * @return The desire chain that failed to resolve.
     */
    public DesireChain getDesireChain() {
        return desires;
    }
    
    /**
     * Get the desire that failed to resolve.
     *
     * @return The unresolvable desire
     */
    public Desire getDesire() {
        return desires.getCurrentDesire();
    }
    
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder("Unable to satisfy desire ")
                .append(format(desires.getCurrentDesire().getInjectionPoint()));
        List<Pair<Satisfaction, InjectionPoint>> path = context;
        if (!path.isEmpty()) {
            sb.append(" of ")
              .append(path.get(0).getLeft());
        }
        sb.append('\n')
          .append(format(context, desires));
        return sb.toString();
    }
}
