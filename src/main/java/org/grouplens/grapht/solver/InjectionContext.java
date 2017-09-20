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
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.SimpleInjectionPoint;
import org.grouplens.grapht.util.AbstractChain;

import org.jetbrains.annotations.Nullable;

/**
 * <p>
 * InjectionContext represents the current path through the dependency graph to
 * the desire being resolved by
 * {@link BindingFunction#bind(InjectionContext, DesireChain)}. The InjectionContext
 * is most significantly represented as a list of satisfactions and the
 * associated injection point attributes. This list represents the "type path"
 * from the root node in the graph to the previously resolved satisfaction.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectionContext extends AbstractChain<Pair<Satisfaction,InjectionPoint>> {
    private static final long serialVersionUID = 1L;

    /**
     * Construct a singleton injection context.
     * @param satisfaction The satisfaction.
     * @param ip The injection point.
     * @return The injection context.
     */
    public static InjectionContext singleton(Satisfaction satisfaction, InjectionPoint ip) {
        return new InjectionContext(null, satisfaction, ip);
    }

    /**
     * Extend or create an injection context.
     * @param prefix The initial context.
     * @param satisfaction The satisfaction.
     * @param ip The injection point.
     * @return The injection context.
     */
    public static InjectionContext extend(@Nullable InjectionContext prefix, Satisfaction satisfaction, InjectionPoint ip) {
        if (prefix == null) {
            return singleton(satisfaction, ip);
        } else {
            return prefix.extend(satisfaction, ip);
        }
    }

    /**
     * Construct a singleton injection context with no attributes.
     * @param satisfaction The satisfaction.
     * @return The injection context.
     */
    public static InjectionContext singleton(Satisfaction satisfaction) {
        return singleton(satisfaction, new SimpleInjectionPoint(null, satisfaction.getErasedType(), true));
    }

    private InjectionContext(InjectionContext prior, Satisfaction satisfaction, InjectionPoint ip) {
        super(prior, Pair.of(satisfaction, ip));
    }

    /**
     * Create a new context that is updated to have the satisfaction and attribute pushed to the
     * end of its type path. The value cache for the new context will be empty.
     * 
     * @param satisfaction The next satisfaction in the dependency graph
     * @param ip The injection point receiving the satisfaction
     * @return A new context with updated type path
     */
    public InjectionContext extend(Satisfaction satisfaction, InjectionPoint ip) {
        return new InjectionContext(this, satisfaction, ip);
    }

    /**
     * Get everything except the last element of this context.
     *
     * @return Everything except the last element of this context, or {@code null} if the context is
     *         a singleton.
     */
    @Nullable
    public InjectionContext getLeading() {
        return (InjectionContext) previous;
    }
}
