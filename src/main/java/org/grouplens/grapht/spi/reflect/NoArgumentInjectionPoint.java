package org.grouplens.grapht.spi.reflect;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

public class NoArgumentInjectionPoint implements InjectionPoint, Externalizable {
 // "final"
    private Method method;

    /**
     * Create a NoArgumentInjectionPoint that wraps the given no-argument
     * method.
     * 
     * @param method The method to invoke without arguments
     */
    public NoArgumentInjectionPoint(Method method) {
        Preconditions.notNull("method", method);
        if (method.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Method takes arguments: " + method);
        }
        
        this.method = method;
    }
    
    /**
     * Constructor required by {@link Externalizable}.
     */
    public NoArgumentInjectionPoint() { }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    @Override
    public Method getMember() {
        return method;
    }
    
    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public Type getType() {
        return Void.class;
    }
    
    @Override
    public Class<?> getErasedType() {
        return Void.class;
    }

    @Override
    public Attributes getAttributes() {
        return new AttributesImpl();
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NoArgumentInjectionPoint)) {
            return false;
        }
        NoArgumentInjectionPoint p = (NoArgumentInjectionPoint) o;
        return p.method.equals(method) && p.method == method;
    }
    
    @Override
    public int hashCode() {
        return method.hashCode();
    }
    
    @Override
    public String toString() {
        return method.getName() + "()";
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        method = Types.readMethod(in);
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        Types.writeMethod(out, method);
    }
}
