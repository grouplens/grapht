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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.Qualifier;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Pair;

/**
 * ReflectionContextMatcher is a ContextMatcher that matches nodes if the node's
 * type inherits from the matcher's type and if the node's {@link Qualifier}
 * matches the configured {@link QualifierMatcher}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionContextMatcher implements ContextMatcher {
    private final Class<?> type;
    private final QualifierMatcher qualifier;

    /**
     * Create a ReflectionContextMatcher that matches the given type 
     * and any qualifier.
     * 
     * @param type The type to match
     */
    public ReflectionContextMatcher(Class<?> type) {
        this(type, Qualifiers.matchAny());
    }

    /**
     * Create a ReflectionContextMatcher that matches the given type and the
     * given {@link Qualifier}.
     * 
     * @param type The type to match
     * @param {@link Qualifier} The {@link Qualifier} to match
     */
    public ReflectionContextMatcher(Class<?> type, QualifierMatcher qualifier) {
        this.type = type;
        this.qualifier = qualifier;
    }
    
    /**
     * @return The type matched by this matcher
     */
    public Class<?> getMatchedType() {
        return type;
    }
    
    /**
     * @return The {@link QualifierMatcher} matched by this matcher
     */
    public QualifierMatcher getMatchedQualifier() {
        return qualifier;
    }
    
    @Override
    public boolean matches(Pair<Satisfaction, Qualifier> n) {
        // we must check for nulls in case it is a synthetic satisfaction
        if (n.getLeft().getErasedType() != null && type.isAssignableFrom(n.getLeft().getErasedType())) {
            // type is a match, so check the QualifierMatcher
            return qualifier.matches(n.getRight());
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionContextMatcher)) {
            return false;
        }
        ReflectionContextMatcher r = (ReflectionContextMatcher) o;
        return r.type.equals(type) && (r.qualifier == null ? qualifier == null : r.qualifier.equals(qualifier));
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (qualifier == null ? 0 : qualifier.hashCode());
    }
    
    @Override
    public String toString() {
        String q = (qualifier == null ? "" : qualifier + ":");
        return "ReflectionContextMatcher(" + q + type.getSimpleName() + ")";
    }
}
