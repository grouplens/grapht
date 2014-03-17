package org.grouplens.grapht.util;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
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
}
