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
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.reflect.internal.*;

import javax.annotation.Nonnull;
import javax.inject.Provider;

/**
 * Class to construct specific {@link Satisfaction} implementations.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Satisfactions {
    private Satisfactions() {}

    public static Satisfaction type(@Nonnull Class<?> type) {
        return new ClassSatisfaction(type);
    }

    public static Satisfaction nullOfType(@Nonnull Class<?> type) {
        return new NullSatisfaction(type);
    }

    public static Satisfaction instance(@Nonnull Object o) {
        return new InstanceSatisfaction(o);
    }

    public static Satisfaction providerType(@Nonnull Class<? extends Provider<?>> providerType) {
        return new ProviderClassSatisfaction(providerType);
    }

    public static Satisfaction providerInstance(@Nonnull Provider<?> provider) {
        return new ProviderInstanceSatisfaction(provider);
    }
}
