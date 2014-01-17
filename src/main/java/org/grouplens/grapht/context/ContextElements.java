/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.grapht.context;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Satisfaction;

/**
 * Utilities for context matching.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class ContextElements {
    private ContextElements() {}

    /**
     * Match priority constants.
     */
    public static enum MatchPriority {
        TYPE,
        INVERTED,
        ANY
    }

    public static ContextElementMatcher matchAny() {
        return AnyMatcher.INSTANCE;
    }

    public static ContextElementMatcher matchType(Class<?> type) {
        return new TypeElementMatcher(type);
    }

    public static ContextElementMatcher matchType(Class<?> type,  QualifierMatcher qual) {
        return new TypeElementMatcher(type, qual);
    }

    public static ContextElementMatcher invertMatch(ContextElementMatcher matcher) {
        return new InvertedMatcher(matcher);
    }

    private static enum AnyMatcher implements ContextElementMatcher {
        INSTANCE;

        @Override
        public MatchElement apply(Pair<Satisfaction,InjectionPoint> n, int position) {
            return MatchElementImpl.ANY;
        }
    }

    private static class InvertedMatcher implements ContextElementMatcher {
        private static final long serialVersionUID = 1L;

        private final ContextElementMatcher delegate;

        public InvertedMatcher(ContextElementMatcher base) {
            delegate = base;
        }

        @Override
        public MatchElement apply(Pair<Satisfaction, InjectionPoint> elem, int position) {
            MatchElement result = delegate.apply(elem, position);
            if (result == null) {
                return MatchElementImpl.INVERTED;
            } else {
                return null;
            }
        }
    }

    private static enum MatchElementImpl implements MatchElement {
        ANY(MatchPriority.ANY),
        INVERTED(MatchPriority.INVERTED);

        private final MatchPriority priority;

        private MatchElementImpl(MatchPriority p) {
            priority = p;
        }

        @Override
        public MatchPriority getPriority() {
            return priority;
        }

        @Override
        public Integer getTypeDistance() {
            return null;
        }
    }
}
