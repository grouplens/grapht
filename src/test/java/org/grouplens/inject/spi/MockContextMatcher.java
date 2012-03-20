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
package org.grouplens.inject.spi;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;

public class MockContextMatcher implements ContextMatcher {
    private final Class<?> type;
    private final MockRole qualifier;
    
    public MockContextMatcher(Class<?> type) {
        this(type, null);
    }
    
    public MockContextMatcher(Class<?> type, @Nullable MockRole qualifier) {
        this.type = type;
        this.qualifier = qualifier;

    }
    
    @Override
    public boolean matches(Pair<Satisfaction, Qualifier> n) {
        boolean typeMatch = type.isAssignableFrom(n.getLeft().getErasedType());
        boolean qualifierMatch = false;
        
        if (qualifier != null) {
            // this context matches a specific qualifier, so we accept any qualifier
            // that matches it or is a sub-qualifier of it
            MockRole c = (MockRole) n.getRight();
            while(c != null) {
                if (c == qualifier) {
                    // found the match
                    qualifierMatch = true;
                    break;
                }
                
                if (!c.isInheritenceEnabled()) {
                    // qualifier has no parent
                    break;
                }
                c = c.getParent();
            }
        } else {
            // this context matches the default qualifier, so we accept the qualifier
            // if it is null, or eventually inherits the default
            qualifierMatch = true;
            MockRole c = (MockRole) n.getRight();
            while(c != null) {
                if (!c.isInheritenceEnabled()) {
                    // does not extend from the default
                    qualifierMatch = false;
                    break;
                }
                c = c.getParent();
            }
        }
        
        return typeMatch && qualifierMatch;
    }
}
