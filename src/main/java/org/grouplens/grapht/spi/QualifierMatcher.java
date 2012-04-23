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
package org.grouplens.grapht.spi;


/**
 * <p>
 * QualifierMatcher encapsulates the logic used to determine if a BindRule or
 * ContextMatcher match a particular Qualifier. Common qualifier matching rules
 * are:
 * <ol>
 * <li>Any qualifier</li>
 * <li>No qualifier</li>
 * <li>Annotation type</li>
 * <li>Annotation instance equality</li>
 * </ol>
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface QualifierMatcher extends Comparable<QualifierMatcher> {
    /**
     * Return true if this matcher matches the given Qualifier. It can be
     * assumed that the qualifier is not null and was created by the same SPI
     * that constructed this matcher.
     * 
     * @param q The qualifier to match
     * @return True if matched
     */
    boolean matches(Qualifier q);
}
