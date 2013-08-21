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
package org.grouplens.grapht.spi;

import org.apache.commons.lang3.tuple.Pair;

public class MockContextElementMatcher implements ContextElementMatcher {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private final MockQualifierMatcher qualifier;
    private final boolean anchored;

    public MockContextElementMatcher(Class<?> type) {
        this(type, MockQualifierMatcher.any());
    }
    
    public MockContextElementMatcher(Class<?> type, MockQualifierMatcher qualifier) {
        this(type, qualifier, false);
    }

    public MockContextElementMatcher(Class<?> type, boolean anchored) {
        this(type, MockQualifierMatcher.any(), anchored);
    }

    public MockContextElementMatcher(Class<?> type, MockQualifierMatcher qualifier, boolean anchored) {
        this.type = type;
        this.qualifier = qualifier;
        this.anchored = anchored;
    }
    
    @Override
    public boolean matches(Pair<Satisfaction, Attributes> n) {
        Satisfaction sat = n.getLeft();
        boolean typeMatches;
        if (sat == null) {
            typeMatches = type == null;
        } else {
            typeMatches = type.isAssignableFrom(sat.getErasedType());
        }
        return typeMatches && qualifier.matches(n.getRight().getQualifier());
    }

    @Override
    public boolean isAnchored() {
        return anchored;
    }
}
