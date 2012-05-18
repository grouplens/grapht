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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.inject.Qualifier;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Pair;
import org.grouplens.grapht.util.Types;

/**
 * ReflectionContextMatcher is a ContextMatcher that matches nodes if the node's
 * type inherits from the matcher's type and if the node's {@link Qualifier}
 * matches the configured {@link QualifierMatcher}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionContextMatcher implements ContextMatcher, Serializable {
    private static final long serialVersionUID = 1L;
    
    // "final"
    private Class<?> type;
    private QualifierMatcher qualifier;
    
    /**
     * Create a ReflectionContextMatcher that matches the given type 
     * and any qualifier.
     * 
     * @param type The type to match
     * @throws NullPointerException if type is null
     */
    public ReflectionContextMatcher(Class<?> type) {
        this(type, Qualifiers.matchAny());
    }

    /**
     * Create a ReflectionContextMatcher that matches the given type and the
     * given {@link Qualifier}.
     * 
     * @param type The type to match
     * @param qualifier The QualifierMatcher that determines how qualifiers are
     *            matched
     * @throws NullPointerException if type or qualifier is null
     */
    public ReflectionContextMatcher(Class<?> type, QualifierMatcher qualifier) {
        Checks.notNull("type", type);
        Checks.notNull("qualifier matcher", qualifier);

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
    public boolean matches(Pair<Satisfaction, Attributes> n) {
        // we must check for nulls in case it is a synthetic satisfaction
        if (n.getLeft().getErasedType() != null && type.isAssignableFrom(n.getLeft().getErasedType())) {
            // type is a match, so check the QualifierMatcher
            return qualifier.matches(n.getRight().getQualifier());
        }
        
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionContextMatcher)) {
            return false;
        }
        ReflectionContextMatcher r = (ReflectionContextMatcher) o;
        return r.type.equals(type) && r.qualifier.equals(qualifier);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ qualifier.hashCode();
    }
    
    @Override
    public String toString() {
        return "ReflectionContextMatcher(" + qualifier + ":" + type.getSimpleName() + ")";
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        type = Types.readClass(in);
        qualifier = (QualifierMatcher) in.readObject();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        Types.writeClass(out, type);
        out.writeObject(qualifier);
    }
}
