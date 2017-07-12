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
package org.grouplens.grapht.reflect;

import org.grouplens.grapht.reflect.internal.*;

import org.jetbrains.annotations.NotNull;
import javax.inject.Provider;

/**
 * Class to construct specific {@link Satisfaction} implementations.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public final class Satisfactions {
    private Satisfactions() {}

    public static Satisfaction type(@NotNull Class<?> type) {
        return new ClassSatisfaction(type);
    }

    public static Satisfaction nullOfType(@NotNull Class<?> type) {
        return new NullSatisfaction(type);
    }

    public static Satisfaction instance(@NotNull Object o) {
        return new InstanceSatisfaction(o);
    }

    public static Satisfaction providerType(@NotNull Class<? extends Provider<?>> providerType) {
        return new ProviderClassSatisfaction(providerType);
    }

    public static Satisfaction providerInstance(@NotNull Provider<?> provider) {
        return new ProviderInstanceSatisfaction(provider);
    }
}
