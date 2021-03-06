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
import org.grouplens.grapht.util.MemberProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * An injection point that is a parameter to a constructor or method.
 */
public class ParameterInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;
    // transient because we use a serialization proxy
    private final transient Executable member;
    private final transient int parameter;
    private final transient AnnotationHelper annotations;

    /**
     * Create an injection point wrapping a constructor or method parameter.
     *
     * @param mem The constructor or method.
     * @param param The parameter index.
     */
    public ParameterInjectionPoint(@NotNull Executable mem, int param) {
        Preconditions.notNull("ctor method", mem);
        Preconditions.inRange(param, 0, mem.getParameterTypes().length);

        this.annotations = new AnnotationHelper(mem.getParameterAnnotations()[param]);
        member = mem;
        parameter = param;
    }

    /**
     * @return The method or constructor wrapped by this injection point
     */
    @Override @NotNull
    public Executable getMember() {
        return member;
    }

    @Override @NotNull
    public Parameter getElement() {
        return member.getParameters()[parameter];
    }

    /**
     * @return The parameter index of this injection point within the
     *         setter's parameters
     */
    public int getParameterIndex() {
        return parameter;
    }

    @Override
    public boolean isOptional() {
        // we'll check both setter and parameter annotations
        return Types.hasNullableAnnotation(member.getAnnotations()) ||
               Types.hasNullableAnnotation(member.getParameterAnnotations()[parameter]);
    }

    @Override
    public Type getType() {
        return Types.box(member.getGenericParameterTypes()[parameter]);
    }

    @Override
    public Class<?> getErasedType() {
        return Types.box(member.getParameterTypes()[parameter]);
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ParameterInjectionPoint)) {
            return false;
        }
        ParameterInjectionPoint p = (ParameterInjectionPoint) o;
        return p.member.equals(member) && p.parameter == parameter;
    }

    @Override
    public int hashCode() {
        return member.hashCode() ^ (37 * 17 * parameter);
    }

    @Override
    public String toString() {
        // method setFoo(..., @Qual Type argN, ...)
        StringBuilder sb = new StringBuilder();
        sb.append("method ")
          .append(member.getName())
          .append("(");
        if (parameter > 0) {
            sb.append("..., ");
        }
        if (annotations.getQualifier() != null) {
            sb.append(annotations.getQualifier())
              .append(" ");
        }
        sb.append(member.getParameterTypes()[parameter].getName())
          .append(" arg")
          .append(parameter);
        if (parameter < member.getParameterTypes().length) {
            sb.append(", ...");
        }
        sb.append(")");

        return sb.toString();
    }

    private Object writeReplace() {
        return new SerialProxy(member, parameter);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 2L;
        private final MemberProxy member;
        private final int parameterIndex;
        public SerialProxy(Executable m, int pidx) {
            member = MemberProxy.of(m);
            parameterIndex = pidx;
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new ParameterInjectionPoint((Executable) member.resolve(), parameterIndex);
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("no class for " + member.toString());
                ex.initCause(e);
                throw ex;
            } catch (NoSuchMethodException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve " + member.toString());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
