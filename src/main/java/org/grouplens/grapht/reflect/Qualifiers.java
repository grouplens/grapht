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

import com.google.common.collect.Sets;
import org.grouplens.grapht.annotation.AliasFor;
import org.grouplens.grapht.annotation.AllowUnqualifiedMatch;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Qualifier;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Utilities related to Qualifier implementations.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class Qualifiers {
    private static final Logger logger = LoggerFactory.getLogger(Qualifiers.class);
    private Qualifiers() { }

    /**
     * Return true or false whether or not the annotation type represents a
     * {@link Qualifier}
     * 
     * @param type The annotation type
     * @return True if the annotation is a {@link Qualifier} or parameter
     * @throws NullPointerException if the type is null
     */
    public static boolean isQualifier(Class<? extends Annotation> type) {
        return type.getAnnotation(javax.inject.Qualifier.class) != null;
    }

    /**
     * Resolve qualifier aliases, returning the target qualifier.  Aliases are resolved
     * recursively.
     *
     * @param type The annotation type.
     * @return The annotation type for which this type is an alias, or {@code type} if it is not an
     * alias.
     * @throws java.lang.IllegalArgumentException if there is a problem with the type, such as a
     *                                            circular alias reference.
     */
    @Nonnull
    public static Class<? extends Annotation> resolveAliases(@Nonnull Class<? extends Annotation> type) {
        Preconditions.notNull("qualifier type", type);
        Set<Class<? extends Annotation>> seen = Sets.newHashSet();
        seen.add(type);
        Class<? extends Annotation> result = type;
        AliasFor alias;
        while ((alias = result.getAnnotation(AliasFor.class)) != null) {
            if (result.getDeclaredMethods().length > 0) {
                throw new IllegalArgumentException("aliased qualifier cannot have parameters");
            }
            result = alias.value();
            if (!result.isAnnotationPresent(Qualifier.class)) {
                throw new IllegalArgumentException("alias target " + type + " is not a qualifier");
            }
            if (!seen.add(result)) {
                throw new IllegalArgumentException("Circular alias reference starting with " + type);
            }
        }
        return result;
    }

    /**
     * The default qualifier matcher. This is currently the {@linkplain #matchAny() any matcher}.
     * @return A QualifierMatcher that matches using the default policy.
     */
    public static QualifierMatcher matchDefault() {
        return new DefaultMatcher();
    }
    
    /**
     * @return A QualifierMatcher that matches any qualifier
     */
    public static QualifierMatcher matchAny() {
        return new AnyMatcher();
    }
    
    /**
     * @return A QualifierMatcher that matches only the null qualifier
     */
    public static QualifierMatcher matchNone() {
        return new NullMatcher();
    }
    
    /**
     * @param annotType Annotation type class to match; {@code null} to match only the lack of a
     *                  qualifier.
     * @return A QualifierMatcher that matches any annotation of the given class
     *         type.
     */
    public static QualifierMatcher match(Class<? extends Annotation> annotType) {
        if (annotType == null) {
            return matchNone();
        } else {
            return new AnnotationClassMatcher(annotType);
        }
    }

    /**
     * @param annot Annotation instance to match, or {@code null} to match only the lack of a qualifier.
     * @return A QualifierMatcher that matches annotations equaling annot
     */
    public static QualifierMatcher match(Annotation annot) {
        if (annot == null) {
            return matchNone();
        } else if (annot.annotationType().getDeclaredMethods().length == 0) {
            logger.debug("using type matcher for nullary annotation {}", annot);
            // Instances of the same nullary annotation are all equal to each other, so just do
            // type checking.  This makes aliasing work with annotation value matchers, b/c we
            // do not allow aliases to have parameters.  The matcher still has value priority.
            return new AnnotationClassMatcher(annot.annotationType(),
                                              DefaultMatcherPriority.MATCH_VALUE);
        } else {
            return new AnnotationMatcher(annot);
        }
    }

    private enum DefaultMatcherPriority {
        MATCH_VALUE,
        MATCH_TYPE,
        MATCH_ANY,
        MATCH_DEFAULT
    }

    private abstract static class AbstractMatcher implements QualifierMatcher {
        private static final long serialVersionUID = 1L;
        private final DefaultMatcherPriority priority;

        AbstractMatcher(DefaultMatcherPriority prio) {
            priority = prio;
        }

        @Override
        public final int getPriority() {
            return priority.ordinal();
        }

        @Override
        @Deprecated
        public boolean matches(Annotation q) {
            return apply(q);
        }

        @Override
        public int compareTo(QualifierMatcher o) {
            if (o == null) {
                // other type is unknown, so extend it to the front
                return 1;
            } else {
                // lower priorities sort lower (higher precedence)
                return getPriority() - o.getPriority();
            }
        }
    }

    private static class DefaultMatcher extends AbstractMatcher {
        private static final long serialVersionUID = 1L;

        DefaultMatcher() {
            super(DefaultMatcherPriority.MATCH_DEFAULT);
        }

        @Override
        public boolean apply(Annotation q) {
            if (q == null) {
                return true;
            } else {
                Class<? extends Annotation> atype = q.annotationType();
                return atype.isAnnotationPresent(AllowUnqualifiedMatch.class);
            }
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof DefaultMatcher;
        }

        @Override
        public int hashCode() {
            return DefaultMatcher.class.hashCode();
        }

        @Override
        public String toString() {
            return "%";
        }
    }
    
    private static class AnyMatcher extends AbstractMatcher {
        private static final long serialVersionUID = 1L;

        AnyMatcher() {
            super(DefaultMatcherPriority.MATCH_ANY);
        }

        @Override
        public boolean apply(Annotation q) {
            return true;
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof AnyMatcher;
        }
        
        @Override
        public int hashCode() {
            return AnyMatcher.class.hashCode();
        }
        
        @Override
        public String toString() {
            return "*";
        }
    }
    
    private static class NullMatcher extends AbstractMatcher {
        private static final long serialVersionUID = 1L;

        NullMatcher() {
            super(DefaultMatcherPriority.MATCH_VALUE);
        }

        @Override
        public boolean apply(Annotation q) {
            return q == null;
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof NullMatcher;
        }
        
        @Override
        public int hashCode() {
            return NullMatcher.class.hashCode();
        }
        
        @Override
        public String toString() {
            return "-";
        }
    }
    
    static class AnnotationClassMatcher extends AbstractMatcher {
        private static final long serialVersionUID = -1L;
        private final Class<? extends Annotation> type;
        private final Class<? extends Annotation> actual;

        public AnnotationClassMatcher(Class<? extends Annotation> type) {
            this(type, DefaultMatcherPriority.MATCH_TYPE);
        }
        
        public AnnotationClassMatcher(Class<? extends Annotation> type,
                                      DefaultMatcherPriority prio) {
            super(prio);
            Preconditions.notNull("type", type);
            Preconditions.isQualifier(type);
            this.type = type;
            // find the actual type to match (resolving aliases)
            actual = resolveAliases(type);
        }
        
        @Override
        public boolean apply(Annotation q) {
            // We test if the alias-resolved types match.
            Class<? extends Annotation> qtype = (q == null ? null : q.annotationType());
            if (qtype == null) {
                return false;
            } else {
                Class<? extends Annotation> qact = resolveAliases(qtype);
                return actual.equals(qact);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof AnnotationClassMatcher
                   && ((AnnotationClassMatcher) o).actual.equals(actual);
        }
        
        @Override
        public int hashCode() {
            return actual.hashCode();
        }
        
        @Override
        public String toString() {
            if (type.equals(actual)) {
                return type.toString();
            } else {
                return type.toString() + "( alias of " + actual.toString() + ")";
            }
        }

        private Object writeReplace() {
            // We just serialize the type. If its alias status changes, that is fine.
            return new SerialProxy(type);
        }

        private void readObject(ObjectInputStream stream) throws ObjectStreamException {
            throw new InvalidObjectException("must use serialization proxy");
        }

        static class SerialProxy implements Serializable {
            private static final long serialVersionUID = 1L;

            private final ClassProxy type;

            public SerialProxy(Class<?> cls) {
                type = ClassProxy.of(cls);
            }

            private Object readResolve() throws ObjectStreamException {
                try {
                    return new AnnotationClassMatcher(type.resolve().asSubclass(Annotation.class));
                } catch (ClassNotFoundException e) {
                    InvalidObjectException ex = new InvalidObjectException("cannot resolve " + type);
                    ex.initCause(e);
                    throw ex;
                } catch (ClassCastException e) {
                    InvalidObjectException ex =
                            new InvalidObjectException("class " + type + " not an annotation");
                    ex.initCause(e);
                    throw ex;
                }
            }
        }
    }
    
    private static class AnnotationMatcher extends AbstractMatcher implements Serializable {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("squid:S1948") // serializable warning; annotations are serializable
        private final Annotation annotation;
        
        public AnnotationMatcher(Annotation annot) {
            super(DefaultMatcherPriority.MATCH_VALUE);
            Preconditions.notNull("annotation", annot);
            Preconditions.isQualifier(annot.annotationType());
            annotation = annot;
        }
        
        @Override
        public boolean apply(Annotation q) {
            return annotation.equals(q);
        }
        
        @Override
        public boolean equals(Object o) {
            return (o instanceof AnnotationMatcher)
                   && ((AnnotationMatcher) o).annotation.equals(annotation);
        }
        
        @Override
        public int hashCode() {
            return annotation.hashCode();
        }
        
        @Override
        public String toString() {
            return annotation.toString();
        }
    }
}
