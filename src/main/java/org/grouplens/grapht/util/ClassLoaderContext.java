package org.grouplens.grapht.util;

/**
 * A class loader context, used to restore the state from {@link ClassLoaders#pushContext(ClassLoader)}.
 *
 * @since 0.8.1
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ClassLoaderContext {
    private final Thread thread;
    private final ClassLoader original;
    private boolean popped = false;

    ClassLoaderContext(Thread th, ClassLoader orig) {
        thread = th;
        original = orig;
    }

    /**
     * Restore the original class loader before this context was entered.
     * @throws IllegalStateException if the context was already popped.
     */
    public void pop() {
        if (popped) {
            throw new IllegalStateException("loader context already popped");
        }
        thread.setContextClassLoader(original);
        popped = true;
    }
}
