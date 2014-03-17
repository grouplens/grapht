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
