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

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.ContextElementMatcher;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;

import javax.inject.Qualifier;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * ReflectionContextElementMatcher is a ContextElementMatcher that matches nodes if the node's
 * type inherits from the matcher's type and if the node's {@link Qualifier}
 * matches the configured {@link QualifierMatcher}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ReflectionContextElementMatcher implements ContextElementMatcher, Serializable {
    private static final long serialVersionUID = -1L;

    private final transient Class<?> type;
    private final transient QualifierMatcher qualifier;
    private final transient boolean anchored;

    /**
     * Create an unanchored ReflectionContextElementMatcher that matches the given type
     * with the default qualifier matcher.
     * 
     * @param type The type to match
     * @throws NullPointerException if type is null
     */
    public ReflectionContextElementMatcher(Class<?> type) {
        this(type, Qualifiers.matchDefault());
    }

    /**
     * Create an unanchored ReflectionContextElementMatcher that matches the given type and the
     * given {@link Qualifier}.
     *
     * @param type      The type to match
     * @param qualifier The QualifierMatcher that determines how qualifiers are matched
     * @throws NullPointerException if type or qualifier is null
     */
    public ReflectionContextElementMatcher(Class<?> type, QualifierMatcher qualifier) {
        this(type, qualifier, false);
    }

    /**
     * Create a ReflectionContextElementMatcher that matches the given type and the given {@link
     * Qualifier}.
     *
     * @param type      The type to match
     * @param qualifier The QualifierMatcher that determines how qualifiers are matched
     * @param anchored  Whether the element matcher is anchored (see {@link #isAnchored()})
     * @throws NullPointerException if type or qualifier is null
     */
    public ReflectionContextElementMatcher(Class<?> type, QualifierMatcher qualifier,
                                           boolean anchored) {
        Preconditions.notNull("type", type);
        Preconditions.notNull("qualifier matcher", qualifier);

        this.type = type;
        this.qualifier = qualifier;
        this.anchored = anchored;
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
    public boolean isAnchored() {
        return anchored;
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

    private Object writeReplace() {
        return new SerialProxy(type, qualifier, anchored);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ClassProxy type;
        private final QualifierMatcher qualifier;
        private final boolean anchored;

        public SerialProxy(Class<?> t, QualifierMatcher qual, boolean anch) {
            type = ClassProxy.of(t);
            qualifier = qual;
            anchored = anch;
        }

        @SuppressWarnings("unchecked")
        private Object readResolve() throws ObjectStreamException {
            try {
                return new ReflectionContextElementMatcher(type.resolve(),
                                                           qualifier,
                                                           anchored);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("cannot resolve " + type);
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
