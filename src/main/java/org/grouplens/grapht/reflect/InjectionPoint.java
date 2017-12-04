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
package org.grouplens.grapht.reflect;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collection;


/**
 * InjectionPoint represents a point of injection for an instantiable type.
 * Examples include a constructor parameter, a setter method, or a field.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface InjectionPoint extends Serializable {
    /**
     * Return the type required to satisfy the injection point.
     *
     * @return The type of the injection point
     */
    Type getType();

    /**
     * @return Return the erased type of {@link #getType()}
     */
    Class<?> getErasedType();

    /**
     * Return the qualifier annotation added to the injection point. The
     * returned annotation's type will have been annotated with
     * {@link javax.inject.Qualifier}. If the injection point is not qualified, this will
     * return null.
     *
     * @return Any qualifier applied to the injection point
     */
    @Nullable
    Annotation getQualifier();

    /**
     * Return the attribute of type A that is applied to the injection point. If
     * the injection point does not have an attribute of type {@code A}, then null is
     * returned.
     *
     * @param atype Attribute annotation type.  It must be annotated with {@link org.grouplens.grapht.annotation.Attribute}.
     * @return The instance of A applied to the injection point, or null
     * @throws NullPointerException if atype is null
     */
    @Nullable <A extends Annotation> A getAttribute(Class<A> atype);

    /**
     * @return Immutable collection of attribute annotations (does not include
     *         the qualifier)
     */
    @NotNull
    Collection<Annotation> getAttributes();

    /**
     * Return the Member that produced this injection point. Synthetic injection
     * points can have a null member.
     *
     * @return The Member that produces this injection point
     */
    @Nullable Member getMember();

    /**
     * Return the element (parameter, field, etc.) of this injection point.
     * @return The injection point element.
     */
    default @Nullable AnnotatedElement getElement() {
        return null;
    }

    /**
     * Transform an object into the actual type needed for the injection point.  The default implementation is the
     * identity function.
     *
     * @param obj The object to transform.
     * @return The transformed object.
     */
    default Object transform(Object obj) {
        return obj;
    }

    /**
     * Get the parameter index, if applicable, for this injection point.
     * @return The parameter index.
     */
    default int getParameterIndex() {
        return -1;
    }

    /**
     * Query whether this injection point is optional.  Optional injection points do not need to be satisfied in order
     * for the class to be instantiated.
     *
     * @return True if this injection point is optional.
     */
    boolean isOptional();
}
