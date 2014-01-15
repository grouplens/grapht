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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.annotation.AllowUnqualifiedMatch;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;

import javax.annotation.Nonnull;
import javax.inject.Qualifier;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 * Utilities related to Qualifier implementations.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class Qualifiers {
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
    public static QualifierMatcher match(@Nonnull Annotation annot) {
        if (annot == null) {
            return matchNone();
        } else {
            return new AnnotationMatcher(annot);
        }
    }
    
    private abstract static class AbstractMatcher implements QualifierMatcher {
        private static final long serialVersionUID = 1L;

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

        @Override
        public int getPriority() {
            return 3;
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

        @Override
        public int getPriority() {
            return 2;
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

        @Override
        public int getPriority() {
            return 0;
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
    
    private static class AnnotationClassMatcher extends AbstractMatcher {
        private static final long serialVersionUID = -1L;
        private final Class<? extends Annotation> type;
        
        public AnnotationClassMatcher(Class<? extends Annotation> type) {
            Preconditions.notNull("type", type);
            Preconditions.isQualifier(type);
            this.type = type;
        }
        
        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public boolean apply(Annotation q) {
            Class<? extends Annotation> qtype = (q == null ? null : q.annotationType());
            return type.equals(qtype);
        }
        
        @Override
        public boolean equals(Object o) {
            return o instanceof AnnotationClassMatcher
                   && ((AnnotationClassMatcher) o).type.equals(type);
        }
        
        @Override
        public int hashCode() {
            return type.hashCode();
        }
        
        @Override
        public String toString() {
            return type.toString();
        }

        private Object writeReplace() {
            return new SerialProxy(type);
        }

        private void readObject(ObjectInputStream stream) throws ObjectStreamException {
            throw new InvalidObjectException("must use serialization proxy");
        }

        private static class SerialProxy implements Serializable {
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

        private final Annotation annotation;
        
        public AnnotationMatcher(Annotation annot) {
            Preconditions.notNull("annotation", annot);
            Preconditions.isQualifier(annot.annotationType());
            annotation = annot;
        }
        
        @Override
        public int getPriority() {
            return 0;
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
