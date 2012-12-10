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
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Qualifier;

import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * Utilities related to Qualifier implementations.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
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
     * @param annotType Annotation type class to match
     * @return A QualifierMatcher that matches any annotation of the given class
     *         type
     */
    public static QualifierMatcher match(Class<? extends Annotation> annotType) {
        return new AnnotationClassMatcher(annotType);
    }
    
    /**
     * @param annot Annotation instance to match
     * @return A QualifierMatcher that matches annotations equaling annot
     */
    public static QualifierMatcher match(Annotation annot) {
        return new AnnotationMatcher(annot);
    }
    
    // These priorities specify that:
    // AnyMatcher < AnnotationClassMatcher < NullMatcher == AnnotationMatcher
    private static final Map<Class<? extends QualifierMatcher>, Integer> TYPE_PRIORITIES;
    static {
        Map<Class<? extends QualifierMatcher>, Integer> tp = new HashMap<Class<? extends QualifierMatcher>, Integer>();
        tp.put(AnyMatcher.class, 0);
        tp.put(AnnotationClassMatcher.class, 1);
        tp.put(NullMatcher.class, 2);
        tp.put(AnnotationMatcher.class, 2);
        
        TYPE_PRIORITIES = Collections.unmodifiableMap(tp);
    }
    
    private static abstract class AbstractMatcher implements QualifierMatcher {
        private static final long serialVersionUID = 1L;

        @Override
        public int compareTo(QualifierMatcher o) {
            if (o == null) {
                // other type is unknown, so push it to the front
                return 1;
            } else {
                // lower priorities sort lower (higher precedence)
                return getPriority() - o.getPriority();
            }
        }
    }
    
    private static class AnyMatcher extends AbstractMatcher {
        private static final long serialVersionUID = 1L;

        @Override
        public int getPriority() {
            return 2;
        }

        @Override
        public boolean matches(Annotation q) {
            return true;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnyMatcher)) {
                return false;
            }
            return true;
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
        public boolean matches(Annotation q) {
            return q == null;
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NullMatcher)) {
                return false;
            }
            return true;
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
    
    private static class AnnotationClassMatcher extends AbstractMatcher implements Externalizable {
        // "final"
        private Class<? extends Annotation> type;
        
        public AnnotationClassMatcher(Class<? extends Annotation> type) {
            Preconditions.notNull("type", type);
            Preconditions.isQualifier(type);
            this.type = type;
        }
        
        /**
         * Constructor required by {@link Externalizable}.
         */
        @SuppressWarnings("unused")
        public AnnotationClassMatcher() { }

        @Override
        public int getPriority() {
            return 1;
        }

        @Override
        public boolean matches(Annotation q) {
            Class<? extends Annotation> qtype = (q == null ? null : q.annotationType());
            return type.equals(qtype);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationClassMatcher)) {
                return false;
            }
            return ((AnnotationClassMatcher) o).type.equals(type);
        }
        
        @Override
        public int hashCode() {
            return type.hashCode();
        }
        
        @Override
        public String toString() {
            return type.toString();
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            type = (Class<? extends Annotation>) Types.readClass(in);
        }
        
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            Types.writeClass(out, type);
        }
    }
    
    private static class AnnotationMatcher extends AbstractMatcher implements Externalizable {
        // "final"
        private Annotation annot;
        
        public AnnotationMatcher(Annotation annot) {
            Preconditions.notNull("annotation", annot);
            Preconditions.isQualifier(annot.annotationType());
            this.annot = annot;
        }
        
        /**
         * Constructor required by {@link Externalizable}.
         */
        @SuppressWarnings("unused")
        public AnnotationMatcher() { }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public boolean matches(Annotation q) {
            return annot.equals(q);
        }
        
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof AnnotationMatcher)) {
                return false;
            }
            return ((AnnotationMatcher) o).annot.equals(annot);
        }
        
        @Override
        public int hashCode() {
            return annot.hashCode();
        }
        
        @Override
        public String toString() {
            return annot.toString();
        }
        
        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            annot = (Annotation) in.readObject();
        }
        
        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(annot);
        }
    }
}
