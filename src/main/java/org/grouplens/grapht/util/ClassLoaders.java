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
