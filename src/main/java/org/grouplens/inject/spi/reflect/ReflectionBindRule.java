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

import javax.annotation.Nullable;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.types.Types;

/**
 * ReflectionBindRule is an abstract implementation of BindRule. It is a partial
 * function from desires to desires. Its matching logic only depends on the
 * source type and Qualifier of the rule, and not what the function produces. A
 * ReflectionBindRule will only match a desire if the desire's desired type
 * equals the source type, and only if the desire's Qualifier inherits from the
 * Qualifier of the bind rule.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public abstract class ReflectionBindRule implements BindRule {
    private final AnnotationQualifier qualifier;
    private final Class<?> sourceType;
    
    private final int weight;

    /**
     * Create a bind rule that matches a desire when the desired type equals
     * <tt>sourceType</tt> and the desire's qualifier inherits from <tt>qualifier</tt>.
     * <tt>weight</tt> is an integer value that specifies the priority between
     * matching bind rules. Lower weights have a higher priority.
     * 
     * @param sourceType The source type this bind rule matches
     * @param qualifier The Qualifier the bind rule applies to
     * @param weight The weight or precedence of the rule
     * @throws NullPointerException if sourceType is null
     */
    public ReflectionBindRule(Class<?> sourceType, @Nullable AnnotationQualifier qualifier, int weight) {
        if (sourceType == null) {
            throw new NullPointerException("Source type cannot be null");
        }
        
        this.qualifier = qualifier;
        this.sourceType = Types.box(sourceType);
        this.weight = weight;
    }

    /**
     * @return The weight or precedence of the rule
     */
    @Override
    public int getWeight() {
        return weight;
    }
    
    /**
     * @return The annotation {@link Qualifier} matched by this bind rule
     */
    public AnnotationQualifier getQualifier() {
        return qualifier;
    }
    
    /**
     * @return The source type matched by this bind rule
     */
    public Class<?> getSourceType() {
        return sourceType;
    }
    
    @Override
    public boolean matches(Desire desire) {
        ReflectionDesire rd = (ReflectionDesire) desire;
        // bind rules match type by equality
        if (rd.getDesiredType().equals(sourceType)) {
            // if the type is equal, then the qualifiers match if
            // the desire's qualifier is inherits from the bind rule's qualifier
            return AnnotationQualifier.inheritsQualifier(rd.getQualifier(), qualifier);
        }
        
        // the type and {@link Qualifier}s are not a match, so return false
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ReflectionBindRule)) {
            return false;
        }
        ReflectionBindRule r = (ReflectionBindRule) o;
        return r.weight == weight && (r.qualifier == null ? qualifier == null : r.qualifier.equals(qualifier)) && r.sourceType.equals(sourceType);
    }
    
    @Override
    public int hashCode() {
        return sourceType.hashCode() ^ (qualifier == null ? 0 : qualifier.hashCode()) ^ weight;
    }
}
