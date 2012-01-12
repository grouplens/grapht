/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.inject.resolver.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Role;
import org.grouplens.inject.spi.Satisfaction;

import com.google.common.base.Function;

// There is a good chance that this will be needed because I think that 
// contextComparator() will be shared by most satisfaction implementations.
abstract class ReflectionSatisfaction implements Satisfaction {
    
    @Override
    public Comparator<ContextMatcher> contextComparator(Role role) {
        // TODO Auto-generated method stub
        return null;
    }
}
