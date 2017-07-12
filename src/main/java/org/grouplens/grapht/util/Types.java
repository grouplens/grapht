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
package org.grouplens.grapht.util;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;

/**
 * Static helper methods for working with types.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class Types {

    private static final TypeVariable<?> PROVIDER_TYPE_VAR =Provider.class.getTypeParameters()[0];

    private Types() {}

    private static final Class<?>[] PRIMITIVE_TYPES = {
        boolean.class, char.class,
        byte.class, short.class, int.class, long.class,
        double.class, float.class
    };

    /**
     * Get a list of unique methods of the class and its superclasses.  This resolves overrides,
     * preferring the subclass implementation.
     *
     * @param type The type to search.
     * @return The list of unique methods.
     */
    public static List<Method> getUniqueMethods(Class<?> type) {
        List<Method> methods = new ArrayList<>();
        // Must keep track of methods overridden in the subtypes.
        Set<Signature> visitedMethods = new HashSet<>();
        while(type != null) {
            for (Method m : type.getDeclaredMethods()) {
                Signature s = new Signature(m);
                if (!visitedMethods.contains(s)) {
                    methods.add(m);
                    visitedMethods.add(s);
                }
            }

            type = type.getSuperclass();
        }

        return methods;
    }

    /**
     * Get all fields from a class and its superclasses.
     * @param type The class.
     * @return The list of fields from the class and its superclasses.
     */
    public static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        while (type != null) {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
            type = type.getSuperclass();
        }
        return fields;
    }

    /**
     * Comparator that sorts classes by inheritance order, superclasses first.
     * @return A comparator that orders classes by inheritance order.
     */
    public static Comparator<Class<?>> supertypesFirst() {
        return (c1, c2) -> {
            if (c1.equals(c2)) {
                return 0;
            } else if (c1.isAssignableFrom(c2)) {
                // c1 is superclass of c2, sort it first
                return -1;
            } else if (c2.isAssignableFrom(c1)) {
                return 1;
            } else {
                throw new IllegalArgumentException("cannot compare " + c1 + " and " + c2);
            }
        };
    }

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
                throw new IllegalArgumentException("raw type not a Class", e);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Return the type distance between the child and parent types. The child type
     * must be a subtype of the parent. The type distance between a class and itself is 0;
     * the distance from a class to one of its immediate supertypes (superclass or a directly
     * implemented interface) is 1; deeper distances are computed recursively.
     * 
     * @param child The child type
     * @param parent The parent type
     * @return The type distance
     * @throws IllegalArgumentException if {@code child} is not a subtype of {@code parent}.
     */
    public static int getTypeDistance(@NotNull Class<?> child, @NotNull Class<?> parent) {
        Preconditions.notNull("child class", child);
        Preconditions.notNull("parent class", parent);

        if (child.equals(parent)) {
            // fast-path same-class tests
            return 0;
        } else if (!parent.isAssignableFrom(child)) {
            // if child does not extend from the parent, return -1
            throw new IllegalArgumentException("child not a subclass of parent");
        } else if (!parent.isInterface()) {
            // if the parent is not an interface, we only need to follower superclasses
            int distance = 0;
            Class<?> cur = child;
            while (!cur.equals(parent)) {
                distance++;
                cur = cur.getSuperclass();
            }
            return distance;
        } else {
            // worst case, recursively compute the type
            // recursion is safe, as types aren't too deep except in crazy-land
            int minDepth = Integer.MAX_VALUE;
            Class<?> sup = child.getSuperclass();
            if (sup != null && parent.isAssignableFrom(sup)) {
                minDepth = getTypeDistance(sup, parent);
            }
            for (Class<?> iface: child.getInterfaces()) {
                if (parent.isAssignableFrom(iface)) {
                    int d = getTypeDistance(iface, parent);
                    if (d < minDepth) {
                        minDepth = d;
                    }
                }
            }
            // minDepth now holds the depth of the superclass with shallowest depth
            return minDepth + 1;
        }
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
        com.google.common.base.Preconditions.checkArgument(Provider.class.isAssignableFrom(providerClass),
                                                           "class is not Provider class");
        Map<TypeVariable<?>, Type> bindings = TypeUtils.getTypeArguments(providerClass, Provider.class);
        Type boundType = bindings.get(PROVIDER_TYPE_VAR);

        if(boundType == null || boundType instanceof TypeVariable){
            throw new IllegalArgumentException("Class provided by " + providerClass.getName() + " is generic");
        }
        final Class<?> inferredType = TypeUtils.getRawType(bindings.get(PROVIDER_TYPE_VAR), null);
        try{
            final Class<?> observedType = providerClass.getMethod("get").getReturnType();
            if (inferredType != null && inferredType.isAssignableFrom(observedType)) {
                return observedType;
            } else {
                return inferredType;
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class does not implement get()", e);
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
        if (provider instanceof TypedProvider) {
            return ((TypedProvider) provider).getProvidedType();
        } else {
            return getProvidedType((Class<? extends Provider<?>>) provider.getClass());
        }
    }
    
    /**
     * Return true if the type is not abstract and not an interface, and has
     * a constructor annotated with {@link Inject} or its only constructor
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
     * Infer a default class loader.
     * @return A reasonable default class loader.
     * @deprecated Use {@link org.grouplens.grapht.util.ClassLoaders#inferDefault()} instead.
     */
    @Deprecated
    public static ClassLoader getDefaultClassLoader() {
        return ClassLoaders.inferDefault();
    }

    /*
     * Internal class to track a methods signature. Java's default reflection
     * doesn't give us a convenient way to record just this information.
     *
     * FIXME Document why we need this class more clearly
     */
    private static class Signature {
        private final String name;
        private final Type[] args;

        public Signature(Method m) {
            // FIXME Make it clearer what this code is supposed to do
            int mods = m.getModifiers();
            if (Modifier.isPublic(mods) || Modifier.isProtected(mods)) {
                // method overrides depends solely on method name
                name = m.getName();
            } else if (Modifier.isPrivate(mods)) {
                // method overrides depend on method name and class name
                name = m.getName() + m.getDeclaringClass().getCanonicalName();
            } else {
                // method overrides depend on method name and package,
                // since it is package-private
                Package pkg = m.getDeclaringClass().getPackage();
                if (pkg != null) {
                    name = m.getName() + pkg.getName();
                } else {
                    name = m.getName();
                }
            }
            args = m.getGenericParameterTypes();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Signature)) {
                return false;
            }
            Signature s = (Signature) o;
            return s.name.equals(name) && Arrays.equals(args, s.args);
        }

        @Override
        public int hashCode() {
            return (name.hashCode() ^ Arrays.hashCode(args));
        }
    }
}
