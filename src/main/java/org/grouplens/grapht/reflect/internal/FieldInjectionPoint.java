/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.FieldProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * FieldInjectionPoint is an injection point wrapping a field.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class FieldInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;
    // transient because we use a serialization proxy
    private final transient Field field;
    private final transient AnnotationHelper annotations;

    /**
     * Create an injection point wrapping the given field
     *
     * @param field The field to inject
     * @throws NullPointerException if field is null
     */
    public FieldInjectionPoint(@NotNull Field field) {
        Preconditions.notNull("field", field);
        this.field = field;
        annotations = new AnnotationHelper(field.getAnnotations());
    }

    @Override
    public Type getType() {
        return Types.box(field.getGenericType());
    }

    @Override
    public Class<?> getErasedType() {
        return Types.box(field.getType());
    }

    @Nullable
    @Override
    public Annotation getQualifier() {
        return annotations.getQualifier();
    }

    @Nullable
    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return annotations.getAttribute(atype);
    }

    @NotNull
    @Override
    public Collection<Annotation> getAttributes() {
        return annotations.getAttributes();
    }

    @Override @NotNull
    public Field getMember() {
        return field;
    }

    @Override @NotNull
    public AnnotatedElement getElement() {
        return field;
    }

    @Override
    public boolean isOptional() {
        return Types.hasNullableAnnotation(field.getAnnotations());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FieldInjectionPoint)) {
            return false;
        }
        return ((FieldInjectionPoint) o).field.equals(field);
    }

    @Override
    public int hashCode() {
        return field.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("field ");
        if (getQualifier() != null) {
            sb.append(getQualifier())
              .append(" ");
        }
        sb.append(field.getType().toString())
          .append(" ")
          .append(field.getName());
        return sb.toString();
    }

    private Object writeReplace() {
        return new SerialProxy(field);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final FieldProxy field;

        public SerialProxy(Field f) {
            field = FieldProxy.of(f);
        }

        private Object readResolve() throws InvalidObjectException {
            try {
                return new FieldInjectionPoint(field.resolve());
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("no class for " + field.toString());
                ex.initCause(e);
                throw ex;
            } catch (NoSuchFieldException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve " + field.toString());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
