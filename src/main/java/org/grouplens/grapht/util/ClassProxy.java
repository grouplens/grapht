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
    private transient volatile Class<?> theClass;

    private ClassProxy(String name) {
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
     * @throws ClassNotFoundException if the class represented by this proxy cannot be found.
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
