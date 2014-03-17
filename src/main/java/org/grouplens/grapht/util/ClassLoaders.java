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

/**
 * Utility methods for class loaders.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 * @since 0.8.1
 */
public final class ClassLoaders {
    private ClassLoaders() {}

    /**
     * Infer a default class loader.
     * @return A reasonable default class loader.
     */
    public static ClassLoader inferDefault() {
        return inferDefault(ClassLoaders.class);
    }

    /**
     * Infer a default class loader.
     * @param refClass a reference class to use for looking up the class loader.  If there is not
     *                 a context class loader on the current thread, the class loader used to load
     *                 this class is returned.
     * @return A reasonable default class loader.
     */
    public static ClassLoader inferDefault(Class<?> refClass) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = refClass.getClassLoader();
        }
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }
        return loader;
    }

    /**
     * Use the specified class loader for the current thread's context class loader.  This method
     * returns a context to make it easy to restore the original class loader.  Example usage:
     *
     * {@code
     * ClassLoaderContext context = ClassLoaders.pushContext(classLoader);
     * try {
     *     // some code with classLoader as the current loader
     * } finally {
     *     context.pop();
     * }
     * }
     *
     * @param loader The class loader to use.
     * @return A context for restoring the original class loader.
     */
    public static ClassLoaderContext pushContext(ClassLoader loader) {
        return pushContext(Thread.currentThread(), loader);
    }

    /**
     * Use the specified class loader for the given thread's context class loader.
     *
     * @param thread The thread whose class loader should be modified.
     * @param loader The class loader to use.
     * @return A context for restoring the original class loader.
     * @see #pushContext(ClassLoader)
     */
    public static ClassLoaderContext pushContext(Thread thread, ClassLoader loader) {
        ClassLoader orig = thread.getContextClassLoader();
        ClassLoaderContext context = new ClassLoaderContext(thread, orig);
        thread.setContextClassLoader(loader);
        return context;
    }
}
