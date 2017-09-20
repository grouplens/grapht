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

import com.google.common.collect.ImmutableList;
import org.grouplens.grapht.ResolutionException;
import org.grouplens.grapht.reflect.Desire;

import java.util.Collection;

/**
 * Thrown when a BindingFunction would be required to return multiple binding
 * results for a given desire and context. This is not thrown when multiple
 * functions are each capable of producing a single result for a desire, since
 * binding functions are given a priority.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class MultipleBindingsException extends ResolutionException {
    private static final long serialVersionUID = 1L;

    private final InjectionContext context;
    private final DesireChain desires;
    private final ImmutableList<?> bindings;
    
    public MultipleBindingsException(DesireChain desires, InjectionContext context, Collection<?> bindings) {
        this.desires = desires;
        this.context = context;
        this.bindings = ImmutableList.copyOf(bindings);
    }
    
    /**
     * @return The context that produced the problematic bindings
     */
    public InjectionContext getContext() {
        return context;
    }
    
    /**
     * @return The possible bindings, which depends on the BindingFunction that
     * produced this exception
     */
    public Collection<?> getBindRules() {
        return bindings;
    }
    
    /**
     * @return The desire that had too many possible binding within a
     *         BindingFunction
     */
    public Desire getDesire() {
        return desires.getCurrentDesire();
    }
    
    @Override
    public String getMessage() {
        return new StringBuilder("Too many choices for desire: ")
            .append(format(getDesire().getInjectionPoint()))
            .append('\n')
            .append(format(context, desires))
            .toString();
    }
}
