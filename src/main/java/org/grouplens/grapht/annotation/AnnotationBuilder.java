/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht.annotation;

import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * AnnotationBuilder is a "builder" for creating proxy Annotation instances.
 * This is useful when configuring dependency injection to match a qualifier
 * annotation that defines attributes, such as the {@link Named} qualifier. As
 * an example, AnnotationBuilder can be used to construct a Named instance that
 * matches particular applications of that annotation:
 * 
 * <pre>
 * Named proxy = new AnnotationBuilder&lt;Named&gt;(Named.class)
 *      .set(&quot;value&quot;, &quot;name&quot;)
 *      .build();
 * </pre>
 * <p>
 * The above snippet creates an instance of Named that returns the String,
 * "name", from its value() method. All other methods declared by
 * {@link Annotation} are implemented correctly given the values configured by
 * the builder, or the defaults declared in the annotation definition.
 * <p>
 * This lets developers define attribute-based qualifiers easily without being
 * forced to provide an actual annotation implementation that can be used to
 * create instances.
 * <p>The proxies returned by this builder are immutable and serializable, like
 * those returned by {@link java.lang.reflect.AnnotatedElement}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @param <T> The annotation type created
 */
public final class AnnotationBuilder<T extends Annotation> {
    private final Map<String, Object> attributes;
    private final Class<T> type;
    
    /**
     * Create a new AnnotationBuilder without any assigned values, that will
     * create annotations of the given class type.
     * 
     * @param annotType The annotation class type
     * @throws NullPointerException if annotType is null
     * @throws IllegalArgumentException if annotType is not an Annotation class
     */
    public AnnotationBuilder(Class<T> annotType) {
        if (annotType == null) {
            throw new NullPointerException("Annotation type cannot be null");
        }
        if (!annotType.isAnnotation()) {
            throw new IllegalArgumentException("Class type is not an Annotation: " + annotType);
        }
        
        type = annotType;
        attributes = new HashMap<String, Object>();
    }

    /**
     * Constructor method to allow the builder type to be inferred.
     * @param annotType The annotation type to build.
     * @param <T> The type of annotation to build (type parameter).
     * @return An annotation builder to create an implementation of the annotation.
     */
    public static <T extends Annotation> AnnotationBuilder<T> of(Class<T> annotType) {
        return new AnnotationBuilder<T>(annotType);
    }
    
    /**
     * Set the annotation defined member given by <tt>name</tt> to the boolean
     * <tt>value</tt>.
     * 
     * @param name The name of the annotation attribute or member to assign
     * @param value The value to assign to the specified member
     * @return This builder
     * @throws NullPointerException if name is null
     * @throws IllegalArgumentException if name is not a defined member in the
     *             annotation type for this builder, or if the type is not
     *             boolean
     */
    public AnnotationBuilder<T> set(String name, boolean value) {
        return set(name, Boolean.valueOf(value), boolean.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a byte value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, byte value) {
        return set(name, Byte.valueOf(value), byte.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a short value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, short value) {
        return set(name, Short.valueOf(value), short.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns an int value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, int value) {
        return set(name, Integer.valueOf(value), int.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a long value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, long value) {
        return set(name, Long.valueOf(value), long.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a char value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, char value) {
        return set(name, Character.valueOf(value), char.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a float value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, float value) {
        return set(name, Float.valueOf(value), float.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a double value.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, double value) {
        return set(name, Double.valueOf(value), double.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a String value. Throws a
     * NullPointerException if value is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, String value) {
        return set(name, value, String.class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns an Annotation instance to
     * the value. A NullPointerException is thrown if value is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, Annotation value) {
        return set(name, value, value.getClass());
    }

    /**
     * As {@link #set(String, boolean)} but assigns a boolean[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, boolean[] value) {
        return set(name, value, boolean[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a byte[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, byte[] value) {
        return set(name, value, byte[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a short[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, short[] value) {
        return set(name, value, short[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a int[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, int[] value) {
        return set(name, value, int[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a long[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, long[] value) {
        return set(name, value, long[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a char[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, char[] value) {
        return set(name, value, char[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a float[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, float[] value) {
        return set(name, value, float[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a double[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, double[] value) {
        return set(name, value, double[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns a String[] value. A
     * NullPointerException is thrown if the array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, String[] value) {
        return set(name, value, String[].class);
    }
    
    /**
     * As {@link #set(String, boolean)} but assigns an Annotation array to the
     * value. The array must be a proper array over the parameterized type, and
     * not a cast of Object[], etc. A NullPointerException is thrown if the
     * array is null.
     * 
     * @param name
     * @param value
     * @return This builder
     */
    public <A extends Annotation> AnnotationBuilder<T> set(String name, A[] value) {
        return set(name, value, value.getClass());
    }

    /**
     * As {@link #set(String, Enum)} but assigns an Annotation instance to
     * the value. A NullPointerException is thrown if value is null.
     *
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, Enum<? extends Enum> value) {
        return set(name, value, value.getClass());
    }

    /**
     * As {@link #set(String, Class)} but assigns an Annotation instance to
     * the value. A NullPointerException is thrown if value is null.
     *
     * @param name
     * @param value
     * @return This builder
     */
    public AnnotationBuilder<T> set(String name, Class<? extends Class> value) {
        return set(name, value, value.getClass());
    }

    /**
     * Set the 'value' attribute to the given boolean value.
     * 
     * @param value The boolean value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(boolean value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given byte value.
     * 
     * @param value The byte value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(byte value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given short value.
     * 
     * @param value The short value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(short value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given int value.
     * 
     * @param value The int value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(int value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given long value.
     * 
     * @param value The long value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(long value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given double value.
     * 
     * @param value The double value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(double value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given float value.
     * 
     * @param value The float value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(float value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given char value.
     * 
     * @param value The char value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(char value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given String value.
     * 
     * @param value The String value
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(String value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given annotation.
     * 
     * @param value The annotation value
     * @return This builder
     */
    public <A extends Annotation> AnnotationBuilder<T> setValue(A value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given boolean array.
     * 
     * @param value The boolean array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(boolean[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given byte array.
     * 
     * @param value The byte array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(byte[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given short array.
     * 
     * @param value The short array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(short[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given int array.
     * 
     * @param value The int array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(int[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given long array.
     * 
     * @param value The long array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(long[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given double array.
     * 
     * @param value The double array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(double[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given float array.
     * 
     * @param value The float array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(float[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given char array.
     * 
     * @param value The char array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(char[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given String array.
     * 
     * @param value The String array
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(String[] value) {
        return set("value", value);
    }
    
    /**
     * Set the 'value' attribute to the given annotation array.
     * 
     * @param value The annotation array
     * @return This builder
     */
    public <A extends Annotation> AnnotationBuilder<T> setValue(A[] value) {
        return set("value", value);
    }

    /**
     * Set the 'value' attribute to the given enum.
     *
     * @param value The enum type annotation
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(Enum<? extends Enum> value) { return set("value", value);}

    /**
     * Set the 'value' attribute to the given class
     *
     * @param value The class type annotation
     * @return This builder
     */
    public AnnotationBuilder<T> setValue(Class<? extends Class> value) { return set("value", value);}



    private AnnotationBuilder<T> set(String name, Object value, Class<?> type) {
        try {
            Method attr = this.type.getMethod(name);
            if (!attr.getReturnType().isAssignableFrom(type)) {
                throw new IllegalArgumentException("Attribute named: " + name + " expects a type of " + attr.getReturnType() + ", but got " + type);
            }
            
            // if valid, save for later
            attributes.put(name, AnnotationProxy.copyAnnotationValue(value));
            return this;
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Annotation type " + this.type + " does not have an attribute named: " + name);
        }
    }
    
    /**
     * Build an Annotation instance of type T that is configured to return the
     * values assigned by the various set() methods for its defined attributes.
     * If attributes have a default value and the value was not overridden by
     * the builder's configuration, then the default will be returned by the
     * annotation instance.
     * 
     * @return An instance of T with the attribute values specified on this
     *         builder
     * @throws IllegalStateException if there are attributes with no default
     *             that have not been assigned explicit values
     */
    public T build() {
        for (Method attr: type.getDeclaredMethods()) {
            if (attr.getDefaultValue() == null) {
                // this is a required value, so we have to 
                // verify that its been assigned
                if (!attributes.containsKey(attr.getName())) {
                    throw new IllegalStateException("No value assigned to required attribute: " + attr.getName());
                }
            }
        }
        return type.cast(Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { type }, 
                                                new AnnotationProxy<T>(type, attributes)));
    }
}
