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

import net.jcip.annotations.ThreadSafe;
import org.jetbrains.annotations.NotNull;

import javax.inject.Provider;

/**
 * MemoizingProvider is a Provider that enforces memoization or caching on
 * another Provider that it wraps.
 *
 * @param <T>
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
@ThreadSafe
public class MemoizingProvider<T> implements TypedProvider<T> {
    private final Provider<T> wrapped;

    // We track a boolean because this supports providing null instances, in
    // which case we can't just check against null to see if we've already
    // queried the base provider
    private volatile T cached;
    private volatile boolean invoked;

    public MemoizingProvider(@NotNull Provider<T> provider) {
        Preconditions.notNull("provider", provider);
        wrapped = provider;
        cached = null;
        invoked = false;
    }

    @Override
    public Class<?> getProvidedType() {
        return Types.getProvidedType(wrapped);
    }

    @Override
    public T get() {
        if (!invoked) {
            synchronized (this) {
                if (!invoked) {
                    cached = wrapped.get();
                    invoked = true;
                }
            }
        }
        return cached;
    }
}
