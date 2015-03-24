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

import java.lang.annotation.*;

/**
 * Marks a qualifier annotation as an alias for another qualifier.  Useful for deprecating
 * qualifiers.
 * <p>
 * The aliased qualifier (the one bearing this annotation) is treated exactly as the qualifier it
 * aliases.  Aliases are recursive and transitive, and the alias annotations are followed until an
 * unaliased qualifier is found.  Circular alias loops are not allowed.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface AliasFor {
    /**
     * Get the target of this alias annotation.
     * @return The annotation for which the annotated annotation is an alias.
     */
    Class<? extends Annotation> value();
}
