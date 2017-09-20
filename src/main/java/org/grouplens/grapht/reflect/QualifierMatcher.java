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
package org.grouplens.grapht.reflect;

import com.google.common.base.Predicate;
import org.jetbrains.annotations.Nullable;

import javax.inject.Qualifier;
import java.io.Serializable;
import java.lang.annotation.Annotation;


/**
 * <p>
 * QualifierMatcher encapsulates the logic used to determine if a BindRule or
 * ContextElementMatcher match a particular Qualifier. Common qualifier matching rules
 * are:
 * <ol>
 * <li>Any qualifier</li>
 * <li>No qualifier</li>
 * <li>Annotation type</li>
 * <li>Annotation instance equality</li>
 * </ol>
 * All QualifierMatchers created by the same InjectSPI must be comparable,
 * matchers from different SPIs do not need to be comparable.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface QualifierMatcher extends Predicate<Annotation>, Comparable<QualifierMatcher>, Serializable {
    /**
     * Return true if this matcher matches the given qualifier annotation. It
     * can be assumed that the annotation type has been annotated with
     * {@link Qualifier}. The qualifier will be null if the injection point
     * being matched did not have a qualifier.
     * 
     * @param q The qualifier to match
     * @return True if matched
     */
    boolean matches(@Nullable Annotation q);

    /**
     * Return true if this matcher matches the given qualifier annotation. It
     * can be assumed that the annotation type has been annotated with
     * {@link Qualifier}. The qualifier will be null if the injection point
     * being matched did not have a qualifier.
     *
     * @param q The qualifier to match
     * @return True if matched
     */
    @Override
    boolean apply(@Nullable Annotation q);

    /**
     * Get the priority of this matcher. Lower priority values have precedence in selecting
     * the final bind rule. All Grapht matchers have priorities of at least 0; negative
     * priority values are reserved for custom extensions.
     *
     * @return The priority.
     */
    int getPriority();
}
