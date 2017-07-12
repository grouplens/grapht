/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
