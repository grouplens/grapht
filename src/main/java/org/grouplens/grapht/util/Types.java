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

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Static helper methods for working with types.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class Types {
    private Types() {}

    private static final Class<?>[] PRIMITIVE_TYPES = {
        boolean.class, char.class,
        byte.class, short.class, int.class, long.class,
        double.class, float.class
    };
    
    /**
     * Create a parameterized type wrapping the given class and type arguments.
     * 
     * @param type
     * @param arguments
     * @return
     */
    public static Type parameterizedType(Class<?> type, Type... arguments) {
        return new ParameterizedTypeImpl(type, arguments);
    }

    /**
     * Return the boxed version of the given type if the type is primitive.
     * Otherwise, if the type is not a primitive the original type is returned.
     * As an example, int.class is converted to Integer.class, but List.class is
     * unchanged. This version of box preserves generics.
     * 
     * @param type The possibly unboxed type
     * @return The boxed type
     */
    public static Type box(Type type) {
        if (type instanceof Class) {
            return box((Class<?>) type);
        } else {
            return type;
        }
    }
    
    /**
     * Return the boxed version of the given type if the type is primitive.
     * Otherwise, if the type is not primitive the original class is returned.
     * 
     * @param type The possibly unboxed type
     * @return The boxed type
     */
    public static Class<?> box(Class<?> type) {
        if (int.class.equals(type)) {
            return Integer.class;
        } else if (short.class.equals(type)) {
            return Short.class;
        } else if (byte.class.equals(type)) {
            return Byte.class;
        } else if (long.class.equals(type)) {
            return Long.class;
        } else if (boolean.class.equals(type)) {
            return Boolean.class;
        } else if (char.class.equals(type)) {
            return Character.class;
        } else if (float.class.equals(type)) {
            return Float.class;
        } else if (double.class.equals(type)) {
            return Double.class;
        } else {
            return type;
        }
    }

    /**
     * Compute the erasure of a type.
     * 
     * @param type The type to erase.
     * @return The class representing the erasure of the type.
     * @throws IllegalArgumentException if <var>type</var> is unerasable (e.g.
     *             it is a type variable or a wildcard).
     */
    public static Class<?> erase(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type raw = pt.getRawType();
            try {
                return (Class<?>) raw;
            } catch (ClassCastException e) {
                throw new RuntimeException("raw type not a Class", e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return the type distance between the child and parent types. If the child
     * does not extend from parent, then a negative value is returned.
     * Otherwise, the number of steps between child and parent is returned. As
     * an example, if child is an immediate subclass of parent, then 1 is
     * returned. If child and parent are equal than 0 is returned.
     * 
     * @param child The child type
     * @param parent The parent type
     * @return The type distance
     * @throws NullPointerException if child or parent are null
     */
    public static int getTypeDistance(Class<?> child, Class<?> parent) {
        if (!parent.isAssignableFrom(child)) {
            // if child does not extend from the parent, return -1
            return -1;
        }
        
        // at this point we can assume at some point a superclass of child
        // will equal parent
        int distance = 0;
        while(!child.equals(parent)) {
            distance++;
            child = child.getSuperclass();
        }
        return distance;
    }
    
    /**
     * Get the type that is provided by a given implementation of
     * {@link Provider}.
     * 
     * @param providerClass The provider's class
     * @return The provided class type
     * @throws IllegalArgumentException if the class doesn't actually implement
     *             Provider
     */
    public static Class<?> getProvidedType(Class<? extends Provider<?>> providerClass) {
        try {
            return Types.box(providerClass.getMethod("get").getReturnType());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement get()");
        }
    }

    /**
     * Get the type that is provided by the Provider instance.
     * 
     * @param provider The provider instance queried
     * @return The provided class type
     * @see #getProvidedType(Class)
     */
    @SuppressWarnings("unchecked")
    public static Class<?> getProvidedType(Provider<?> provider) {
        return getProvidedType((Class<? extends Provider<?>>) provider.getClass());
    }
    
    /**
     * Return true if the type is not abstract and not an interface, and has
     * a constructor annotated with {@link Inject}, or its only constructor
     * is the default constructor.
     * 
     * @param type A class type
     * @return True if the class type is instantiable
     */
    public static boolean isInstantiable(Class<?> type) {
        if (!Modifier.isAbstract(type.getModifiers()) && !type.isInterface()) {
            // first check for a constructor annotated with @Inject, 
            //  - this doesn't care how many we'll let the injector complain
            //    if there are more than one
            for (Constructor<?> c: type.getDeclaredConstructors()) {
                if (c.getAnnotation(Inject.class) != null) {
                    return true;
                }
            }
            
            // check if we only have the public default constructor
            if (type.getConstructors().length == 1 
                && type.getConstructors()[0].getParameterTypes().length == 0) {
                return true;
            }
        }
        
        // no constructor available
        return false;
    }
    
    /**
     * <p>
     * Return true if the type is not abstract and not an interface. This will
     * return true essentially when the class "should" have a default
     * constructor or a constructor annotated with {@link Inject @Inject} to be
     * used properly.
     * <p>
     * As another special rule, if the input type is {@link Void}, false is
     * returned because for most intents and purposes, it is not instantiable.
     * 
     * @param type The type to test
     * @return True if it should be instantiable
     */
    public static boolean shouldBeInstantiable(Class<?> type) {
        return !Modifier.isAbstract(type.getModifiers()) && !type.isInterface() && !Void.class.equals(type);
    }
    
    /**
     * Return true if the array of Annotations contains an Annotation with a
     * simple name of 'Nullable'. It does not matter which actual Nullable
     * annotation is present.
     * 
     * @param annotations Array of annotations, e.g. from a setter or
     *            constructor
     * @return True if there exists a Nullable annotation in the array
     */
    public static boolean hasNullableAnnotation(Annotation[] annotations) {
        for (Annotation a: annotations) {
            if (a.annotationType().getSimpleName().equals("Nullable")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Load a class by name, even if it is a primitive type..
     * @param name The name of the class.
     * @return The class.
     * @throws ClassNotFoundException
     */
    @Nonnull
    private static Class<?> classByName(@Nonnull String name) throws ClassNotFoundException {
        for (Class<?> cls: PRIMITIVE_TYPES) {
            if (cls.getName().equals(name)) {
                return cls;
            }
        }
        return Class.forName(name);
    }
    
    /**
     * Read in a Class from the given ObjectInput. This is only compatible with
     * classes that were serialized with
     * {@link #writeClass(ObjectOutput, Class)}. Although Class is Serializable,
     * this guarantees a simple structure within a file.
     * 
     * @param in The stream to read from
     * @return The next Class encoded in the stream
     * @throws IOException if an IO error occurs
     * @throws ClassNotFoundException if the class can no longer be found at
     *             runtime
     */
    public static Class<?> readClass(ObjectInput in) throws IOException, ClassNotFoundException {
        String typeName = in.readUTF();
        int arrayCount = in.readInt();
        int hash = in.readInt();

        Class<?> baseType = classByName(typeName);
        if (hash != hash(baseType)) {
            throw new IOException("Class definition changed since serialization: " + typeName);
        }
        
        if (arrayCount > 0) {
            return Array.newInstance(baseType, new int[arrayCount]).getClass();
        } else {
            return baseType;
        }
    }
    
    /**
     * <p>
     * Write the Class to the given ObjectOutput. When the class type is not an
     * array, its canonical name is written as a UTF string, and a false boolean
     * to record that it's not an array. When it is an array type, the canonical
     * name of its component type is written as a UTF string, and then a true
     * boolean value.
     * <p>
     * The class can be decoded by calling {@link #readClass(ObjectInput)}.
     * 
     * @param out The stream to write to
     * @param cls The class type to encode
     * @throws IOException if an IO error occurs
     */
    public static void writeClass(ObjectOutput out, Class<?> cls) throws IOException {
        int arrayCount = 0;
        Class<?> baseType = cls;
        while(baseType.isArray()) {
            arrayCount++;
            baseType = baseType.getComponentType();
        }
        
        out.writeUTF(baseType.getCanonicalName());
        out.writeInt(arrayCount);
        out.writeInt(hash(baseType));
    }
    
    /**
     * Read in a Constructor from the given ObjectInput. This is only compatible
     * with constructors serialized
     * {@link #writeConstructor(ObjectOutput, Constructor)}. Because Constructor
     * is not Serializable, this must be used instead of
     * {@link ObjectInput#readObject()}.
     * 
     * @param in The stream to read from
     * @return The next constructor encoded in the stream
     * @throws IOException if an IO error occurs
     * @throws ClassNotFoundException if the declaring class of the constructor
     *             cannot be found at runtime
     * @throws NoSuchMethodException if the constructor no longer exists in the
     *             loaded class definition
     */
    public static Constructor<?> readConstructor(ObjectInput in) throws IOException, ClassNotFoundException {
        Class<?> declaring = readClass(in);
        Class<?>[] args = new Class<?>[in.readInt()];
        for (int i = 0; i < args.length; i++) {
            args[i] = readClass(in);
        }
        
        try {
            return declaring.getDeclaredConstructor(args);
        } catch (NoSuchMethodException e) {
            throw new IOException("Constructor no longer exists", e);
        }
    }
    
    /**
     * <p>
     * Write the Constructor to the given ObjectOutput. Because Constructor is
     * not Serializable, it is encoded to the stream as its declaring class, the
     * number of parameters, and then the parameter classes in their defined
     * order. All classes are written to the stream using
     * {@link #writeClass(ObjectOutput, Class)}.
     * <p>
     * The constructor can be decoded by calling
     * {@link #readConstructor(ObjectInput)}.
     * 
     * @param out The output stream to write to
     * @param ctor The constructor to serialize
     * @throws IOException if an IO error occurs
     */
    public static void writeConstructor(ObjectOutput out, Constructor<?> ctor) throws IOException {
        writeClass(out, ctor.getDeclaringClass());
        
        Class<?>[] args = ctor.getParameterTypes();
        out.writeInt(args.length);
        for (int i = 0; i < args.length; i++) {
            writeClass(out, args[i]);
        }
    }
    
    /**
     * Read in a Method from the given ObjectInput. This is only compatible with
     * methods serialized {@link #writeMethod(ObjectOutput, Method)}. Because
     * Method is not Serializable, this must be used instead of
     * {@link ObjectInput#readObject()}.
     * 
     * @param in The stream to read from
     * @return The next method encoded in the stream
     * @throws IOException If an IO error occurs or if the method no longer
     *             exists
     * @throws ClassNotFoundException if the declaring class of the method
     *             cannot be found at runtime
     */
    public static Method readMethod(ObjectInput in) throws IOException, ClassNotFoundException {
        Class<?> declaring = readClass(in);
        String name = in.readUTF();
        
        Class<?>[] args = new Class<?>[in.readInt()];
        for (int i = 0; i < args.length; i++) {
            args[i] = readClass(in);
        }
        
        try {
            return declaring.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new IOException("Method no longer exists", e);
        }
    }
    
    /**
     * <p>
     * Write the Method to the given ObjectOutput. Because Method is not
     * Serializable, it is encoded to the stream as its declaring class, its
     * name as a UTF string, the number of parameters, and then the parameter
     * classes in their defined order. All classes are written to the stream
     * using {@link #writeClass(ObjectOutput, Class)}.
     * <p>
     * The method can be decoded by calling {@link #readMethod(ObjectInput)}.
     * 
     * @param out The output stream to write to
     * @param m The method to serialize
     * @throws IOException if an IO error occurs
     */
    public static void writeMethod(ObjectOutput out, Method m) throws IOException {
        writeClass(out, m.getDeclaringClass());
        out.writeUTF(m.getName());
        
        Class<?>[] args = m.getParameterTypes();
        out.writeInt(args.length);
        for (int i = 0; i < args.length; i++) {
            writeClass(out, args[i]);
        }
    }
    
    /**
     * Read in a Field from the given ObjectInput. This is only compatible with
     * methods serialized {@link #writeField(ObjectOutput, Field)}. Because
     * Method is not Serializable, this must be used instead of
     * {@link ObjectInput#readObject()}.
     * 
     * @param in The stream to read from
     * @return The next field encoded in the stream
     * @throws IOException If an IO error occurs or if the field no longer
     *             exists
     * @throws ClassNotFoundException if the declaring class of the field cannot
     *             be found at runtime
     */
    public static Field readField(ObjectInput in) throws IOException, ClassNotFoundException {
        Class<?> declaring = readClass(in);
        String name = in.readUTF();
        
        Class<?> fieldType = readClass(in);
        
        Field f;
        try {
            f = declaring.getDeclaredField(name);
            if (f.getType().equals(fieldType)) {
                throw new IOException("Field type changed from " + fieldType + " to " + f.getType());
            }
            return f;
        } catch (NoSuchFieldException e) {
            throw new IOException("Field no longer exists", e);
        }
    }
    
    /**
     * <p>
     * Write the Field to the given ObjectOutput. Because Field is not
     * Serializable, it is encoded to the stream as its declaring class, its
     * name as a UTF string, and its type. All classes are written to the stream
     * using {@link #writeClass(ObjectOutput, Class)}.
     * <p>
     * The method can be decoded by calling {@link #readField(ObjectInput)}.
     * 
     * @param out The output stream to write to
     * @param f The field to serialize
     * @throws IOException if an IO error occurs
     */
    public static void writeField(ObjectOutput out, Field f) throws IOException {
        writeClass(out, f.getDeclaringClass());
        out.writeUTF(f.getName());
        writeClass(out, f.getType());
    }
    
    private static int hash(Class<?> type) {
        // convert to a string, both for lexigraphical ordering
        // and to combine into a hash
        List<String> ctors = new ArrayList<String>();
        for (Constructor<?> c: type.getDeclaredConstructors()) {
            ctors.add(c.getName() + ":" + Arrays.toString(c.getParameterTypes()));
        }
        List<String> methods = new ArrayList<String>();
        for (Method m: type.getDeclaredMethods()) {
            methods.add(m.getName() + ":" + Arrays.toString(m.getParameterTypes()));
        }
        List<String> fields = new ArrayList<String>();
        for (Field f: type.getDeclaredFields()) {
            fields.add(f.getName() + ":" + f.getType().getName());
        }
        
        // impose a consistent ordering
        Collections.sort(ctors);
        Collections.sort(methods);
        Collections.sort(fields);
        
        StringBuilder sb = new StringBuilder();
        for (String c: ctors) {
            sb.append(c);
        }
        for (String m: methods) {
            sb.append(m);
        }
        for (String f: fields) {
            sb.append(f);
        }
        
        return sb.toString().hashCode();
    }
}
