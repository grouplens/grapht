package org.grouplens.grapht.annotation;

import org.grouplens.grapht.util.ClassProxy;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Proxy used to implement annotation interfaces.  It implements the {@link Annotation}
 * contract by delegating to a map of named attribute values.  A new AnnotationProxy instance
 * should be created for each proxy annotation.
 *
 * @see {@link AnnotationBuilder}
 */
class AnnotationProxy<T extends Annotation> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1L;
    private final ClassProxy annotationType;
    private final Map<String, Object> attributes;
    private transient Class<T> cachedType;

    public AnnotationProxy(Class<T> type, Map<String, Object> attrs) {
        annotationType = ClassProxy.of(type);
        cachedType = type;
        attributes = Collections.unmodifiableMap(new HashMap<String, Object>(attrs));
    }

    /**
     * Customized {@code readObject} implementation to ensure the cached type is resolved.
     *
     * @param in The stream.
     * @throws java.io.ObjectStreamException If there is an error reading the object from the stream.
     */
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws ObjectStreamException {
        try {
            in.defaultReadObject();
            cachedType = (Class<T>) annotationType.resolve();
        } catch (IOException e) {
            ObjectStreamException ex = new StreamCorruptedException("IO exception");
            ex.initCause(e);
            throw ex;
        } catch (ClassNotFoundException e) {
            ObjectStreamException ex = new InvalidObjectException("IO exception");
            ex.initCause(e);
            throw ex;
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isHashCode(method)) {
            return proxyHashCode(proxy);
        } else if (isEquals(method)) {
            return proxyEquals(proxy, args[0]);
        } else if (isAnnotationType(method)) {
            return proxyAnnotationType();
        } else if (isToString(method)) {
            return proxyToString(proxy);
        } else if (attributes.containsKey(method.getName()) && method.getParameterTypes().length == 0) {
            return AnnotationBuilder.copyAnnotationValue(attributes.get(method.getName()));
        } else {
            // fall back to the default
            return AnnotationBuilder.copyAnnotationValue(method.getDefaultValue());
        }
        // wait() and other Object methods do not get sent to the InvocationHandler
        // so we don't have any other cases
    }

    private boolean isEquals(Method m) {
        return m.getName().equals("equals") && m.getReturnType().equals(boolean.class)
            && m.getParameterTypes().length == 1 && m.getParameterTypes()[0].equals(Object.class);
    }

    private boolean isHashCode(Method m) {
        return m.getName().equals("hashCode") && m.getReturnType().equals(int.class)
            && m.getParameterTypes().length == 0;
    }

    private boolean isAnnotationType(Method m) {
        return m.getName().equals("annotationType") && m.getReturnType().equals(Class.class)
            && m.getParameterTypes().length == 0;
    }

    private boolean isToString(Method m) {
        return m.getName().equals("toString") && m.getReturnType().equals(String.class)
            && m.getParameterTypes().length == 0;
    }

    private Class<? extends Annotation> proxyAnnotationType() {
        return cachedType;
    }

    private String proxyToString(Object o) {
        StringBuilder sb = new StringBuilder("@");
        sb.append(cachedType.getName());
        sb.append('(');

        boolean first = true;
        // the declared methods on an Annotation definition
        // are the attributes considered in the hash code
        for (Method attr: cachedType.getDeclaredMethods()) {
            try {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }

                sb.append(attr.getName());
                sb.append('=');

                Object value = attr.invoke(o);

                if (attr.getReturnType().isArray()) {
                    // the JVM prints out arrays nicely, so we might as well
                    sb.append(arrayToString(value));
                } else {
                    sb.append(value);
                }
            } catch(Exception e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        sb.append(')');
        return sb.toString();
    }

    private int proxyHashCode(Object proxy) {
        // Annotation hash code is the sum of the hashes of its members,
        // where the member hash is (127 * name.hashCode()) ^ value.hashCode()

        int hash = 0;

        // the declared methods on an Annotation definition
        // are the attributes considered in the hash code
        for (Method attr: cachedType.getDeclaredMethods()) {
            try {
                Object value = attr.invoke(proxy);

                // Annotation has specific instructions for the hash code of primitive
                // types, but it amounts to boxing the type, which has already been
                // done for us when members were placed in the attribute map

                // special case for arrays, just as in proxyEquals()
                int valueHash = (attr.getReturnType().isArray() ? arrayHashCode(value)
                                                                : value.hashCode());
                hash += (127 * attr.getName().hashCode()) ^ valueHash;
            } catch (Exception e) {
                // the method reflection should not fail, since Annotation
                // methods must be public and take 0 arguments
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        return hash;
    }

    private boolean proxyEquals(Object o1, Object o2) {
        if (!cachedType.isInstance(o2)) {
            return false;
        }

        // the declared methods on an Annotation definition
        // are the attributes considered in the hash code
        for (Method attr: cachedType.getDeclaredMethods()) {
            try {
                Object v1 = attr.invoke(o1);
                Object v2 = attr.invoke(o2);

                if (v1.getClass().isArray()) {
                    // Annotation mandates special equality behavior for arrays
                    if (!arrayEquals(v1, v2)) {
                        return false;
                    }
                } else {
                    if (!v1.equals(v2)) {
                        return false;
                    }
                }
            } catch(Exception e) {
                throw new RuntimeException("Unexpected exception", e);
            }
        }

        // all attributes equal via Object.equals() or Arrays.equals()
        return true;
    }

    private String arrayToString(Object o1) {
        if (o1 instanceof boolean[]) {
            return Arrays.toString((boolean[]) o1);
        } else if (o1 instanceof byte[]) {
            return Arrays.toString((byte[]) o1);
        } else if (o1 instanceof short[]) {
            return Arrays.toString((short[]) o1);
        } else if (o1 instanceof int[]) {
            return Arrays.toString((int[]) o1);
        } else if (o1 instanceof long[]) {
            return Arrays.toString((long[]) o1);
        } else if (o1 instanceof char[]) {
            return Arrays.toString((char[]) o1);
        } else if (o1 instanceof float[]) {
            return Arrays.toString((float[]) o1);
        } else if (o1 instanceof double[]) {
            return Arrays.toString((double[]) o1);
        } else {
            return Arrays.toString((Object[]) o1);
        }
    }

    private int arrayHashCode(Object o1) {
        if (o1 instanceof boolean[]) {
            return Arrays.hashCode((boolean[]) o1);
        } else if (o1 instanceof byte[]) {
            return Arrays.hashCode((byte[]) o1);
        } else if (o1 instanceof short[]) {
            return Arrays.hashCode((short[]) o1);
        } else if (o1 instanceof int[]) {
            return Arrays.hashCode((int[]) o1);
        } else if (o1 instanceof long[]) {
            return Arrays.hashCode((long[]) o1);
        } else if (o1 instanceof char[]) {
            return Arrays.hashCode((char[]) o1);
        } else if (o1 instanceof float[]) {
            return Arrays.hashCode((float[]) o1);
        } else if (o1 instanceof double[]) {
            return Arrays.hashCode((double[]) o1);
        } else {
            return Arrays.hashCode((Object[]) o1);
        }
    }

    private boolean arrayEquals(Object o1, Object o2) {
        // we assume that o1 and o2 have the same class type at this point
        if (o1 instanceof boolean[]) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
        } else if (o1 instanceof byte[]) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
        } else if (o1 instanceof short[]) {
            return Arrays.equals((short[]) o1, (short[]) o2);
        } else if (o1 instanceof int[]) {
            return Arrays.equals((int[]) o1, (int[]) o2);
        } else if (o1 instanceof long[]) {
            return Arrays.equals((long[]) o1, (long[]) o2);
        } else if (o1 instanceof char[]) {
            return Arrays.equals((char[]) o1, (char[]) o2);
        } else if (o1 instanceof float[]) {
            return Arrays.equals((float[]) o1, (float[]) o2);
        } else if (o1 instanceof double[]) {
            return Arrays.equals((double[]) o1, (double[]) o2);
        } else {
            return Arrays.equals((Object[]) o1, (Object[]) o2);
        }
    }
}
