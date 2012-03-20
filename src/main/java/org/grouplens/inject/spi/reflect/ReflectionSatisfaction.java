/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.spi.reflect;

import java.util.Comparator;

import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.spi.Satisfaction;
import org.grouplens.inject.types.Types;

/**
 * ReflectionSatisfaction is an abstract satisfaction that implements the
 * context comparing logic for its subclasses.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public abstract class ReflectionSatisfaction implements Satisfaction {
    @Override
    public Comparator<ContextMatcher> contextComparator(Qualifier qualifier) {
        final AnnotationQualifier r = (AnnotationQualifier) qualifier;
        return new Comparator<ContextMatcher>() {
            @Override
            public int compare(ContextMatcher o1, ContextMatcher o2) {
                ReflectionContextMatcher cm1 = (ReflectionContextMatcher) o1;
                ReflectionContextMatcher cm2 = (ReflectionContextMatcher) o2;
                
                // #1 - order by type distance, select the matcher that is closest
                int td1 = Types.getTypeDistance(getErasedType(), cm1.getMatchedType());
                int td2 = Types.getTypeDistance(getErasedType(), cm2.getMatchedType());
                if (td1 != td2) {
                    return td1 - td2;
                }
                
                // #2 - order by qualifier distance, select the matcher that is closest
                int rd1 = Qualifiers.getQualifierDistance(r, cm1.getMatchedQualifier());
                int rd2 = Qualifiers.getQualifierDistance(r, cm2.getMatchedQualifier());
                return rd1 - rd2;
            }
        };
    }
}
