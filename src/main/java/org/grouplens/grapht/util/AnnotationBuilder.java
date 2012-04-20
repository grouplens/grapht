package org.grouplens.grapht.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AnnotationBuilder<T extends Annotation> {
    private final Map<String, Object> attributes;
    private final Class<T> type;
    
    public AnnotationBuilder(Class<T> annotType) {
        if (annotType == null) {
            throw new NullPointerException("Annotation type cannot be null");
        }
        
        type = annotType;
        attributes = new HashMap<String, Object>();
    }
    
    public AnnotationBuilder<T> set(String name, boolean value) {
        return set(name, Boolean.valueOf(value), boolean.class);
    }
    
    public AnnotationBuilder<T> set(String name, byte value) {
        return set(name, Byte.valueOf(value), byte.class);
    }
    
    public AnnotationBuilder<T> set(String name, int value) {
        return set(name, Integer.valueOf(value), int.class);
    }
    
    public AnnotationBuilder<T> set(String name, long value) {
        return set(name, Long.valueOf(value), long.class);
    }
    
    public AnnotationBuilder<T> set(String name, char value) {
        return set(name, Character.valueOf(value), char.class);
    }
    
    public AnnotationBuilder<T> set(String name, float value) {
        return set(name, Float.valueOf(value), float.class);
    }
    
    public AnnotationBuilder<T> set(String name, double value) {
        return set(name, Double.valueOf(value), double.class);
    }
    
    public AnnotationBuilder<T> set(String name, String value) {
        return set(name, value, String.class);
    }
    
    public AnnotationBuilder<T> set(String name, Annotation value) {
        return set(name, value, value.getClass());
    }
    
    public AnnotationBuilder<T> set(String name, boolean[] value) {
        return set(name, value, boolean[].class);
    }
    
    public AnnotationBuilder<T> set(String name, byte[] value) {
        return set(name, value, byte[].class);
    }
    
    public AnnotationBuilder<T> set(String name, int[] value) {
        return set(name, value, int[].class);
    }
    
    public AnnotationBuilder<T> set(String name, long[] value) {
        return set(name, value, long[].class);
    }
    
    public AnnotationBuilder<T> set(String name, char[] value) {
        return set(name, value, char[].class);
    }
    
    public AnnotationBuilder<T> set(String name, float[] value) {
        return set(name, value, float[].class);
    }
    
    public AnnotationBuilder<T> set(String name, double[] value) {
        return set(name, value, double[].class);
    }
    
    public AnnotationBuilder<T> set(String name, String[] value) {
        return set(name, value, String[].class);
    }
    
    public <A extends Annotation> AnnotationBuilder<T> set(String name, A[] value) {
        return set(name, value, value.getClass());
    }
    
    private AnnotationBuilder<T> set(String name, Object value, Class<?> type) {
        try {
            Method attr = this.type.getMethod(name);
            if (!attr.getReturnType().equals(type)) {
                throw new IllegalArgumentException("Attribute named: " + name + " expects a type of " + attr.getReturnType() + ", but got " + type);
            }
            
            // if valid, save for later
            attributes.put(name, value);
            return this;
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Annotation type " + this.type + " does not have an attribute named: " + name);
        }
    }
    
    public T build() {
        return type.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { type }, 
                                                new AnnotationProxy<T>(type, attributes)));
    }
    
    private static class AnnotationProxy<T extends Annotation> implements InvocationHandler {
        private final Class<T> annotType;
        private final Map<String, Object> attributes;
        
        public AnnotationProxy(Class<T> annotType, Map<String, Object> attributes) {
            this.annotType = annotType;
            this.attributes = Collections.unmodifiableMap(new HashMap<String, Object>(attributes));
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
                return attributes.get(method.getName());
            } else {
                // fall back to the default
                return method.getDefaultValue();
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
            return annotType;
        }
        
        private String proxyToString(Object o) {
            StringBuilder sb = new StringBuilder("@");
            sb.append(annotType.getName());
            sb.append('(');
            
            boolean first = true;
            // the declared methods on an Annotation definition
            // are the attributes considered in the hash code
            for (Method attr: annotType.getDeclaredMethods()) {
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
            for (Method attr: annotType.getDeclaredMethods()) {
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
            if (!annotType.isInstance(o2)) {
                return false;
            }
            
            // the declared methods on an Annotation definition
            // are the attributes considered in the hash code
            for (Method attr: annotType.getDeclaredMethods()) {
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
}
