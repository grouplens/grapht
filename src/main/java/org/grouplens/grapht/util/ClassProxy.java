package org.grouplens.grapht.util;

import org.apache.commons.lang3.ClassUtils;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

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
public class ClassProxy implements Serializable {
    private static final long serialVersionUID = 1;

    private final String className;
    private volatile transient Class<?> theClass;

    public ClassProxy(String name) {
        className = name;
    }

    /**
     * Get the class name. This name does not include any array information.
     * @return The class name.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Resolve a class proxy to a class.
     * @return The class represented by this proxy.
     */
    public Class<?> resolve() throws ClassNotFoundException {
        if (theClass == null) {
            theClass = ClassUtils.getClass(className);
        }
        return theClass;
    }

    /**
     * Construct a class proxy for a class.
     * @param cls The class.
     * @return The class proxy.
     */
    public static ClassProxy forClass(Class<?> cls) {
        ClassProxy proxy = new ClassProxy(cls.getName());
        proxy.theClass = cls;
        return proxy;
    }
}
