/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
