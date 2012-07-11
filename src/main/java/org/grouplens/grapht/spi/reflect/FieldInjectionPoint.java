package org.grouplens.grapht.spi.reflect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

/**
 * FieldInjectionPoint is an injection point wrapping a field.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class FieldInjectionPoint implements InjectionPoint, Externalizable {
    // "final"
    private Field field;
    
    private transient Attributes attributes;
    
    /**
     * Create an injection point wrapping the given field
     * 
     * @param field
     * @throws NullPointerException if field is null
     */
    public FieldInjectionPoint(Field field) {
        Preconditions.notNull("field", field);
        this.field = field;
        attributes = new AttributesImpl(field.getAnnotations());
    }
    
    /**
     * Constructor required for {@link Externalizable}
     */
    public FieldInjectionPoint() { }
    
    @Override
    public Type getType() {
        return Types.box(field.getGenericType());
    }

    @Override
    public Class<?> getErasedType() {
        return Types.box(field.getType());
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public Field getMember() {
        return field;
    }

    @Override
    public boolean isNullable() {
        return Types.hasNullableAnnotation(field.getAnnotations());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeField(out, field);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        field = Types.readField(in);
        attributes = new AttributesImpl(field.getAnnotations());
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
        return field.toString();
    }
}
