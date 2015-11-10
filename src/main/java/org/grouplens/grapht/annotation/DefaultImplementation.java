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
package org.grouplens.grapht.annotation;

import org.grouplens.grapht.CachePolicy;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * A default implementation for a {@link Qualifier}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Documented
public @interface DefaultImplementation {
    /**
     * The implementation class.
     * @return The implementation class.
     */
    Class<?> value();

    /**
     * The default cache policy of this default.
     * @return The default cache policy for this binding.
     */
    CachePolicy cachePolicy() default CachePolicy.NO_PREFERENCE;

    /**
     * Whether the default binding should be skipped if its dependencies cannot be satisfied.
     * @return {@code true} if this default should be skipped if its dependencies cannot be satisfied.
     */
    boolean skipIfUnusable() default false;
}
