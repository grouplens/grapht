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

import javax.annotation.Nullable;

import org.grouplens.grapht.util.Pair;

public class MockContextMatcher implements ContextMatcher {
    private final Class<?> type;
    private final MockQualifier qualifier;
    
    public MockContextMatcher(Class<?> type) {
        this(type, null);
    }
    
    public MockContextMatcher(Class<?> type, @Nullable MockQualifier qualifier) {
        this.type = type;
        this.qualifier = qualifier;

    }
    
    @Override
    public boolean matches(Pair<Satisfaction, Qualifier> n) {
        boolean typeMatch = type.isAssignableFrom(n.getLeft().getErasedType());
        boolean qualifierMatch = false;
        
        Qualifier c = n.getRight();
        if (c == null && qualifier == null) {
            qualifierMatch = true;
        } else if (qualifier != null) {
            while(c != null) {
                if (c.equals(qualifier)) {
                    // the original child eventually inherits from the parent
                    qualifierMatch = true;
                    break;
                }
                c = c.getParent();
            }
        } else {
            // make sure the child qualifier inherits from the default
            while(c != null) {
                if (c.inheritsDefault()) {
                    // the child inherits the default
                    qualifierMatch = true;
                }
                c = c.getParent();
            }
        }
        
        return typeMatch && qualifierMatch;
    }
}
