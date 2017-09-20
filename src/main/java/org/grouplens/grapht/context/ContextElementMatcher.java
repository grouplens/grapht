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
package org.grouplens.grapht.context;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;

import javax.inject.Qualifier;
import java.io.Serializable;

/**
 * <p>
 * ContextElementMatcher represents a "pattern" that can match an element within the
 * dependency context created as a Resolver follows a dependency hierarchy. The
 * dependency context is an ordered list of satisfactions and the qualifiers of the desires they satisfy.
 * The first satisfaction is the root satisfaction, a {@code null} satisfaction of type {@code void}.
 * <p>
 * ContextMatchers can match or apply to these nodes and {@link Qualifier}s
 * within a dependency context. As an example, the reflection based
 * ContextElementMatcher matches nodes that are sub-types of the type the matcher was
 * configured with.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface ContextElementMatcher extends Serializable {
    /**
     * Return true if this ContextElementMatcher matches or applies to the given Satisfaction and
     * Qualifier.
     *
     * @param n The node and attributes in the current dependency context
     * @return A match if this matcher matches the provided node label, or {@code false} if there is
     *         no match.
     */
    MatchElement apply(Pair<Satisfaction, InjectionPoint> n);
}
