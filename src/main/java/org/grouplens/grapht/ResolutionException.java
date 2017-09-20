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
package org.grouplens.grapht;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.solver.DesireChain;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.solver.SolverException;

/**
 * Exception thrown when there is a dependency resolution error.
 *
 * @since 0.9
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
@SuppressWarnings("deprecation")
public class ResolutionException extends SolverException {
    private static final long serialVersionUID = 1L;

    public ResolutionException() {
        super();
    }

    public ResolutionException(String msg) {
        super(msg);
    }

    public ResolutionException(Throwable throwable) {
        super(throwable);
    }

    public ResolutionException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
    
    protected String format(InjectionContext ctx, DesireChain desires) {
        StringBuilder sb = new StringBuilder();
        
        // type path
        sb.append("Context:\n");
        sb.append("  Type path:\n");
        for (Pair<Satisfaction, InjectionPoint> path: ctx) {
            Satisfaction sat = path.getLeft();
            Class<?> type = sat == null ? null : sat.getErasedType();
            sb.append("    ")
              .append(format(path.getRight(), type))
              .append('\n');
        }
        sb.append('\n');
        
        // desire chain
        sb.append("  Prior desires:\n");
        for (Desire desire: desires.getPreviousDesires()) {
            sb.append("    ")
              .append(format(desire.getInjectionPoint(), desire.getDesiredType()))
              .append('\n');
        }

        return sb.toString();
    }

    protected String format(InjectionPoint ip) {
        return format(ip, ip.getErasedType());
    }

    protected String format(InjectionPoint ip, Class<?> type) {
        if (type == null) {
            type = ip.getErasedType();
        }
        String base = (ip.getQualifier() != null ? ip.getQualifier() + ":" : "");
        String name = type == null ? null : type.getName();
        return base + name;
    }
}
