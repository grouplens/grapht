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

import com.google.common.base.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.inject.Provider;

/**
 * Utility methods for providers.
 *
 * @since 0.6
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public final class Providers {
    private Providers() {}

    public static <T> Provider<T> of(@NotNull T object) {
        return new InstanceProvider<T>(object, object.getClass());
    }

    public static <T> Provider<T> of(@Nullable T object, Class<?> type) {
        return new InstanceProvider<T>(object, type);
    }

    public static <T> Provider<T> memoize(@NotNull Provider<T> inner) {
        return new MemoizingProvider<T>(inner);
    }

    /**
     * Convert a supplier to a provider.
     * @param supplier The supplier.
     * @param type The supplier's return type (to help the injector).
     * @param <T> The type returned from the supplier.
     * @return A provider.
     */
    public static <T> Provider<T> fromSupplier(final Supplier<T> supplier, final Class<T> type) {
        return new TypedProvider<T>() {
            @Override
            public Class<?> getProvidedType() {
                return type;
            }

            @Override
            public T get() {
                return supplier.get();
            }
        };
    }
}
