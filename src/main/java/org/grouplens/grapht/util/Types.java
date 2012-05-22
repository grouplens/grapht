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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Static helper methods for working with types.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public final class Types {
    private Types() {}

    /**
     * Return the boxed version of the given type if the type is primitive.
     * Otherwise, if the type is not a primitive the original type is returned.
     * As an example, int.class is converted to Integer.class.
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
            for (Constructor<?> c: type.getConstructors()) {
                if (c.getAnnotation(Inject.class) != null) {
                    return true;
                }
            }
            
            // check if we only have the default constructor
            if (type.getConstructors().length == 1 
                && type.getConstructors()[0].getParameterTypes().length == 0) {
                return true;
            }
        }
        
        // no constructor available
        return false;
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
     * Read in a Class from the given ObjectInputStream. This is only compatible
     * with classes that were serialized with
     * {@link #writeClass(ObjectOutputStream, Class)}. Although Class is
     * Serializable, this guarantees a simple structure within a file.
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
        
        Class<?> baseType = Class.forName(typeName);
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
     * Write the Class to the given ObjectOutputStream. When the class type is
     * not an array, its canonical name is written as a UTF string, and a false
     * boolean to record that it's not an array. When it is an array type, the
     * canonical name of its component type is written as a UTF string, and then
     * a true boolean value.
     * <p>
     * The class can be decoded by calling {@link #readClass(ObjectInputStream)}.
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
     * Read in a Constructor from the given ObjectInputStream. This is only
     * compatible with constructors serialized
     * {@link #writeConstructor(ObjectOutputStream, Constructor)}. Because
     * Constructor is not Serializable, this must be used instead of
     * {@link ObjectInputStream#readObject()}.
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
     * Write the Constructor to the given ObjectOutputStream. Because
     * Constructor is not Serializable, it is encoded to the stream as its
     * declaring class, the number of parameters, and then the parameter classes
     * in their defined order. All classes are written to the stream using
     * {@link #writeClass(ObjectOutputStream, Class)}.
     * <p>
     * The constructor can be decoded by calling
     * {@link #readConstructor(ObjectInputStream)}.
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
     * Read in a Method from the given ObjectInputStream. This is only
     * compatible with methods serialized
     * {@link #writeMethod(ObjectOutputStream, Method)}. Because Method is not
     * Serializable, this must be used instead of
     * {@link ObjectInputStream#readObject()}.
     * 
     * @param in The stream to read from
     * @return The next method encoded in the stream
     * @throws IOException If an IO error occurs
     * @throws ClassNotFoundException if the declaring class of the method
     *             cannot be found at runtime
     * @throws NoSuchMethodException if the method no longer exists in the
     *             loaded class definition
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
     * Write the Method to the given ObjectOutputStream. Because Method is not
     * Serializable, it is encoded to the stream as its declaring class, its
     * name as a UTF string, the number of parameters, and then the parameter
     * classes in their defined order. All classes are written to the stream
     * using {@link #writeClass(ObjectOutputStream, Class)}.
     * <p>
     * The method can be decoded by calling
     * {@link #readMethod(ObjectInputStream)}.
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
