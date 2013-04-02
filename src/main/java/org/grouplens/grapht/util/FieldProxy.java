package org.grouplens.grapht.util;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Proxy class for serializing fields.
 */
public class FieldProxy implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ClassProxy declaringClass;
    private final String fieldName;
    private final ClassProxy fieldType;
    private transient Field field;

    private FieldProxy(ClassProxy cls, String n, ClassProxy type) {
        declaringClass = cls;
        fieldName = n;
        fieldType = type;
    }

    @Override
    public String toString() {
        return String.format("FieldProxy(%s of %s)", fieldName, declaringClass.getClassName());
    }

    public Field resolve() throws ClassNotFoundException, NoSuchFieldException {
        if (field == null) {
            Class<?> cls = declaringClass.resolve();
            field = cls.getDeclaredField(fieldName);
            // REVIEW Original code verified the field type. Is this necessary?
        }
        return field;
    }

    public static FieldProxy forField(Field field) {
        FieldProxy proxy = new FieldProxy(ClassProxy.forClass(field.getDeclaringClass()),
                                          field.getName(),
                                          ClassProxy.forClass(field.getType()));
        proxy.field = field;
        return proxy;
    }
}
