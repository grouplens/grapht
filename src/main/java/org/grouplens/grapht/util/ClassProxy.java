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

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * A serialization proxy for class instances.  This serializable class encapsulates a simple
 * representation for classes when serializing object graphs.
 * <p>
 *     When using this class, classes are serialized as their binary name, as returned by
 *     {@link Class#getName()}.  The name encodes array information, so this is adequate
 *     to fully reconstruct the class.
 * </p>
 *
 * @author Michael Ekstrand
 */
@Immutable
public final class ClassProxy implements Serializable {
    private static final long serialVersionUID = 1;

    private final String className;
    // checksum stored as 2 longs for serialization ease
    private final long checkWord1;
    private final long checkWord2;
    @Nullable
    private transient volatile WeakReference<Class<?>> theClass;

    private ClassProxy(String name, ByteBuffer checksum) {
        if (checksum.limit() != 16) {
            throw new IllegalArgumentException("checksum array has wrong length");
        }
        className = name;
        checkWord1 = checksum.getLong();
        checkWord2 = checksum.getLong();
    }

    /**
     * Get the class name. This name does not include any array information.
     * @return The class name.
     */
    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return "proxy of " + className;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ClassProxy) {
            ClassProxy op = (ClassProxy) o;
            return className.equals(op.className);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return className.hashCode();
    }

    /**
     * Resolve a class proxy to a class.
     * @return The class represented by this proxy.
     * @throws ClassNotFoundException if the class represented by this proxy cannot be found.
     */
    public Class<?> resolve() throws ClassNotFoundException {
        WeakReference<Class<?>> ref = theClass;
        Class<?> cls = ref == null ? null : ref.get();
        if (cls == null) {
            cls = ClassUtils.getClass(className);
            ByteBuffer check = checksumClass(cls);
            long w1 = check.getLong();
            long w2 = check.getLong();
            if (checkWord1 == w1 && checkWord2 == w2) {
                theClass = new WeakReference<Class<?>>(cls);
            } else {
                throw new ClassNotFoundException("checksum mismatch for " + cls.getName());
            }
        }
        return cls;
    }

    private static final Map<Class<?>, ClassProxy> proxyCache = new WeakHashMap<Class<?>, ClassProxy>();

    /**
     * Construct a class proxy for a class.
     *
     * @param cls The class.
     * @return The class proxy.
     */
    public static synchronized ClassProxy of(Class<?> cls) {
        ClassProxy proxy = proxyCache.get(cls);
        if (proxy == null) {
            proxy = new ClassProxy(cls.getName(), checksumClass(cls));
            proxy.theClass = new WeakReference<Class<?>>(cls);
            proxyCache.put(cls, proxy);
        }
        return proxy;
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /**
     * Compute a checksum for a class. These checksums are used to see if a class has changed
     * its definition since being serialized.
     *
     * @param type The class to checksum.
     * @return A read-only byte buffer containing the class's checksum.
     */
    private static ByteBuffer checksumClass(Class<?> type) {
        // we compute a big hash of all the members of the class, and its superclasses.

        List<String> members = new ArrayList<String>();
        for (Constructor<?> c: type.getDeclaredConstructors()) {
            members.add(String.format("%s(%s)", c.getName(),
                                      StringUtils.join(c.getParameterTypes(), ", ")));
        }
        for (Method m: type.getDeclaredMethods()) {
            members.add(String.format("%s(%s): %s", m.getName(),
                                      StringUtils.join(m.getParameterTypes(), ", "),
                                      m.getReturnType()));
        }
        for (Field f: type.getDeclaredFields()) {
            members.add(f.getName() + ":" + f.getType().getName());
        }

        Collections.sort(members);

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("JVM does not support MD5");
        }
        Class<?> sup = type.getSuperclass();
        if (sup != null) {
            digest.update(checksumClass(sup));
        }
        for (String mem: members) {
            digest.update(mem.getBytes(UTF8));
        }

        return ByteBuffer.wrap(digest.digest()).asReadOnlyBuffer();
    }
}
