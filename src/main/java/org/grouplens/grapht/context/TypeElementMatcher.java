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
package org.grouplens.grapht.context;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Types;

import org.jetbrains.annotations.Nullable;
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
class TypeElementMatcher implements ContextElementMatcher, Serializable {
    private static final long serialVersionUID = -1L;

    @Nullable
    private final transient Class<?> type;
    private final transient QualifierMatcher qualifier;

    /**
     * Create an unanchored ReflectionContextElementMatcher that matches the given type
     * with the default qualifier matcher.
     * 
     * @param type The type to match
     * @throws NullPointerException if type is null
     */
    public TypeElementMatcher(Class<?> type) {
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
    public TypeElementMatcher(Class<?> type, QualifierMatcher qualifier) {
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
    public MatchElement apply(Pair<Satisfaction, InjectionPoint> n) {
        // we must check for nulls in case it is a synthetic satisfaction
        Satisfaction sat = n.getLeft();
        boolean typeMatches;
        if (type == null) {
            typeMatches = sat == null
                          || sat.getErasedType() == null
                          || sat.getType().equals(Void.TYPE);
        } else {
            typeMatches = sat != null && sat.getErasedType() != null &&
                          type.isAssignableFrom(sat.getErasedType());
        }

        if (typeMatches && qualifier.matches(n.getRight().getQualifier())) {
            return new MatchElem(sat == null ? null : sat.getErasedType(),
                                 type, qualifier);
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof TypeElementMatcher) {
            TypeElementMatcher r = (TypeElementMatcher) o;
            return new EqualsBuilder().append(type, r.type)
                                      .append(qualifier, r.qualifier)
                                      .isEquals();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type)
                                    .append(qualifier)
                                    .toHashCode();
    }
    
    @Override
    public String toString() {
        String tname = type == null ? "null" : type.getSimpleName();
        return "[" + qualifier + ":" + tname + "]";
    }

    private static class MatchElem implements MatchElement {
        private final Class<?> matchedType;
        private final Class<?> patternType;
        private final QualifierMatcher qualMatcher;

        private MatchElem(Class<?> mtype, Class<?> ptype, QualifierMatcher qmatch) {
            matchedType = mtype;
            patternType = ptype;
            qualMatcher = qmatch;
        }

        @Override
        public ContextElements.MatchPriority getPriority() {
            return ContextElements.MatchPriority.TYPE;
        }

        @Override
        public Integer getTypeDistance() {
            return Types.getTypeDistance(matchedType, patternType);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o instanceof MatchElem) {
                MatchElem other = (MatchElem) o;
                EqualsBuilder eqb = new EqualsBuilder();
                return eqb.append(matchedType, other.matchedType)
                          .append(patternType, other.patternType)
                          .append(qualMatcher, other.qualMatcher)
                          .isEquals();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            HashCodeBuilder hcb = new HashCodeBuilder();
            return hcb.append(matchedType)
                      .append(patternType)
                      .append(qualMatcher)
                      .toHashCode();
        }

        @Override
        public String toString() {
            return String.format("Match(%s,%s)", matchedType, patternType);
        }
    }

    private Object writeReplace() {
        return new SerialProxy(type, qualifier);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 2L;

        private final ClassProxy type;
        private final QualifierMatcher qualifier;

        public SerialProxy(Class<?> t, QualifierMatcher qual) {
            type = ClassProxy.of(t);
            qualifier = qual;
        }

        @SuppressWarnings("unchecked")
        private Object readResolve() throws ObjectStreamException {
            try {
                return new TypeElementMatcher(type.resolve(),
                                                           qualifier);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("cannot resolve " + type);
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
