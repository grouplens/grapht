/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.util;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import net.jcip.annotations.Immutable;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
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
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
@Immutable
public final class ClassProxy implements Serializable {
    private static final long serialVersionUID = 1;
    private static final Logger logger = LoggerFactory.getLogger(ClassProxy.class);

    private final String className;
    private final long checksum;
    @Nullable
    private transient volatile WeakReference<Class<?>> theClass;
    private transient ClassLoader classLoader;

    private ClassProxy(String name, long check) {
        className = name;
        checksum = check;
        classLoader = ClassLoaders.inferDefault(ClassProxy.class);
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        classLoader = ClassLoaders.inferDefault(ClassProxy.class);
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
            if(className.equals("void")) {
                // special case
                cls = Void.TYPE;
            } else {
                cls = ClassUtils.getClass(classLoader, className);
            }
            long check = checksumClass(cls);
            if (!isSerializationPermissive() && checksum != check) {
                throw new ClassNotFoundException("checksum mismatch for " + cls.getName());
            } else {
                if (checksum != check) {
                    logger.warn("checksum mismatch for {}", cls);
                }
                theClass = new WeakReference<Class<?>>(cls);
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

    public static boolean isSerializationPermissive() {
        return Boolean.getBoolean("grapht.deserialization.permissive");
    }

    /**
     * Compute a checksum for a class. These checksums are used to see if a class has changed
     * its definition since being serialized.
     * <p>
     * The checksum used here is not cryptographically strong. It is intended only as a sanity
     * check to detect incompatible serialization, not to robustly prevent tampering. The
     * checksum algorithm currently is to compute an MD5 checksum over class member signatures
     * and XOR the lower and upper halves of the checksum.
     * </p>
     *
     * @param type The class to checksum.
     * @return The checksum.
     */
    @SuppressWarnings("deprecation")
    private static long checksumClass(Class<?> type) {
        HashFunction hf = Hashing.md5();
        Hasher hash = hf.newHasher();
        checksumClass(type, hash);

        HashCode code = hash.hash();
        ByteBuffer buf = ByteBuffer.wrap(code.asBytes());
        long l1 = buf.getLong();
        long l2 = buf.getLong();
        return l1 ^ l2;
    }

    private static void checksumClass(Class<?> type, Hasher hash) {
        // we compute a big hash of all the members of the class, and its superclasses.

        List<String> members = new ArrayList<String>();
        for (Constructor<?> c: type.getDeclaredConstructors()) {
            if (isInjectionSensitive(c)) {
                members.add(String.format("%s(%s)", c.getName(),
                                          StringUtils.join(c.getParameterTypes(), ", ")));
            }
        }
        for (Method m: type.getDeclaredMethods()) {
            if (isInjectionSensitive(m)) {
                members.add(String.format("%s(%s): %s", m.getName(),
                                          StringUtils.join(m.getParameterTypes(), ", "),
                                          m.getReturnType()));
            }
        }
        for (Field f: type.getDeclaredFields()) {
            if (isInjectionSensitive(f)) {
                members.add(f.getName() + ":" + f.getType().getName());
            }
        }

        Collections.sort(members);

        Class<?> sup = type.getSuperclass();
        if (sup != null) {
            checksumClass(sup, hash);
        }
        for (String mem: members) {
            hash.putString(mem, Charsets.UTF_8);
        }
    }

    /**
     * Check whether a member is injection-sensitive and should be checked for validity in
     * deserialization.
     *
     * @param m The member.
     * @param <M> The type of member (done so we can check multiple types).
     * @return {@code true} if the member should be checksummed, {@code false} to ignore it.
     */
    private static <M extends Member & AnnotatedElement>boolean isInjectionSensitive(M m) {
        // static methods are not sensitive
        if (Modifier.isStatic(m.getModifiers())) {
            return false;
        }

        // private members w/o @Inject are not sensitive
        if (Modifier.isPrivate(m.getModifiers()) && m.getAnnotation(Inject.class) == null) {
            return false;
        }

        // public, protected, or @Inject - it's sensitive (be conservative)
        return true;
    }
}
