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
package org.grouplens.grapht.annotation;

import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Attribute is an auxiliary annotation, like {@link Qualifier}, that can be
 * used to add additional information to injection points (setters,
 * constructors, fields). Unlike qualifiers, attributes do not determine the
 * outcome of bindings, they only add information that is accessible in the
 * final dependency graph.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute { }
