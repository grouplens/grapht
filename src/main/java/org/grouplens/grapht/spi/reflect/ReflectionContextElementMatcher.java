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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.inject.Qualifier;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.ContextElementMatcher;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * ReflectionContextElementMatcher is a ContextElementMatcher that matches nodes if the node's
 * type inherits from the matcher's type and if the node's {@link Qualifier}
 * matches the configured {@link QualifierMatcher}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ReflectionContextElementMatcher implements ContextElementMatcher, Externalizable {
    // "final"
    private Class<?> type;
    private QualifierMatcher qualifier;
    
    /**
     * Create a ReflectionContextElementMatcher that matches the given type
     * and any qualifier.
     * 
     * @param type The type to match
     * @throws NullPointerException if type is null
     */
    public ReflectionContextElementMatcher(Class<?> type) {
        this(type, Qualifiers.matchAny());
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public ReflectionContextElementMatcher() { }

    /**
     * Create a ReflectionContextElementMatcher that matches the given type and the
     * given {@link Qualifier}.
     * 
     * @param type The type to match
     * @param qualifier The QualifierMatcher that determines how qualifiers are
     *            matched
     * @throws NullPointerException if type or qualifier is null
     */
    public ReflectionContextElementMatcher(Class<?> type, QualifierMatcher qualifier) {
        Preconditions.notNull("type", type);
        Preconditions.notNull("qualifier matcher", qualifier);

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
        if (!(o instanceof ReflectionContextElementMatcher)) {
            return false;
        }
        ReflectionContextElementMatcher r = (ReflectionContextElementMatcher) o;
        return r.type.equals(type) && r.qualifier.equals(qualifier);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ qualifier.hashCode();
    }
    
    @Override
    public String toString() {
        return "Context(" + qualifier + ":" + type.getSimpleName() + ")";
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        type = Types.readClass(in);
        qualifier = (QualifierMatcher) in.readObject();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeClass(out, type);
        out.writeObject(qualifier);
    }
}
