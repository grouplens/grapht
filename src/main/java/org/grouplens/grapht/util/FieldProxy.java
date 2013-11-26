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
package org.grouplens.grapht.util;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.annotation.Nullable;
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
