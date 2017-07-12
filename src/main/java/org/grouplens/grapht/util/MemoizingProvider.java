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
