/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.resolver;

import javax.annotation.Nullable;

import org.grouplens.inject.resolver.ContextMatcher;
import org.grouplens.inject.spi.MockRole;
import org.grouplens.inject.spi.SatisfactionAndRole;

public class MockContextMatcher implements ContextMatcher {
    private final Class<?> type;
    private final MockRole role;
    
    public MockContextMatcher(Class<?> type) {
        this(type, null);
    }
    
    public MockContextMatcher(Class<?> type, @Nullable MockRole role) {
        this.type = type;
        this.role = role;

    }
    
    @Override
    public boolean matches(SatisfactionAndRole n) {
        boolean typeMatch = type.isAssignableFrom(n.getSatisfaction().getErasedType());
        boolean roleMatch = false;
        
        if (role != null) {
            // this context matches a specific role, so we accept any role
            // that matches it or is a sub-role of it
            MockRole c = (MockRole) n.getRole();
            while(c != null) {
                if (c == role) {
                    // found the match
                    roleMatch = true;
                    break;
                }
                
                if (!c.isInheritenceEnabled()) {
                    // role has no parent
                    break;
                }
                c = c.getParent();
            }
        } else {
            // this context matches the default role, so we accept the role
            // if it is null, or eventually inherits the default
            roleMatch = true;
            MockRole c = (MockRole) n.getRole();
            while(c != null) {
                if (!c.isInheritenceEnabled()) {
                    // does not extend from the default
                    roleMatch = false;
                    break;
                }
                c = c.getParent();
            }
        }
        
        return typeMatch && roleMatch;
    }
}
