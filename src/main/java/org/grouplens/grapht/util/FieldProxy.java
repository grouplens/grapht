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
package org.grouplens.grapht.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.jetbrains.annotations.Nullable;
import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Proxy class for serializing fields.
 */
public final class FieldProxy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ClassProxy declaringClass;
    private final String fieldName;
    private final ClassProxy fieldType;
    @Nullable
    private transient volatile Field field;
    private transient volatile int hash;

    private FieldProxy(ClassProxy cls, String n, ClassProxy type) {
        declaringClass = cls;
        fieldName = n;
        fieldType = type;
    }

    @Override
    public String toString() {
        return String.format("proxy of %s.%s", declaringClass.getClassName(), fieldName);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof FieldProxy) {
            FieldProxy op = (FieldProxy) o;
            return declaringClass.equals(op.declaringClass)
                    && fieldName.equals(op.fieldName)
                    && fieldType.equals(op.fieldType);
        } else {
            return true;
        }
    }

    @Override
    public int hashCode() {
        if (hash == 0) {
            HashCodeBuilder hcb = new HashCodeBuilder();
            hash = hcb.append(declaringClass)
                      .append(fieldName)
                      .append(fieldType)
                      .hashCode();
        }
        return hash;
    }

    /**
     * Resolve this proxy into a {@link Field} instance.
     * @return The {@link Field} represented by this proxy.
     * @throws ClassNotFoundException If the proxy's declaring type cannot be resolved.
     * @throws NoSuchFieldException If the field does not exist on the declaring type.
     */
    public Field resolve() throws ClassNotFoundException, NoSuchFieldException {
        Field cachedField = field;
        if (cachedField == null) {
            Class<?> cls = declaringClass.resolve();
            field = cachedField = cls.getDeclaredField(fieldName);
        }
        // REVIEW Do we want to test equality or assignability?
        if (!cachedField.getType().equals(fieldType.resolve())) {
            throw new NoSuchFieldException("type mismatch on " + cachedField.toString());
        }
        return cachedField;
    }

    /**
     * Construct a proxy for a field.
     * @param field The field to proxy.
     * @return The field proxy representing {@code field}.
     */
    public static FieldProxy of(Field field) {
        FieldProxy proxy = new FieldProxy(ClassProxy.of(field.getDeclaringClass()),
                                          field.getName(),
                                          ClassProxy.of(field.getType()));
        proxy.field = field;
        return proxy;
    }
}
