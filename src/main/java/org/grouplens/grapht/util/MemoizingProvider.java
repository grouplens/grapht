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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Provider;

/**
 * MemoizingProvider is a Provider that enforces memoization or caching on
 * another Provider that it wraps.
 *
 * @param <T>
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@ThreadSafe
public class MemoizingProvider<T> implements TypedProvider<T> {
    private final Provider<T> wrapped;

    // We track a boolean because this supports providing null instances, in
    // which case we can't just check against null to see if we've already
    // queried the base provider
    private volatile T cached;
    private volatile boolean invoked;

    public MemoizingProvider(@Nonnull Provider<T> provider) {
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
