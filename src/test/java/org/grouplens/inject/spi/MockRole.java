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
package org.grouplens.inject.spi;

import javax.annotation.Nullable;

/**
 * MockRole is a simple Role implementation that represents roles as unique
 * objects. It can map a hierarchy by referring to other parent roles.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockRole implements Role {
    private final MockRole parent;
    private final boolean enableInheritence;
    
    public MockRole() {
        parent = null;
        enableInheritence = false;
    }
    
    public MockRole(@Nullable MockRole parent) {
        this.parent = parent;
        enableInheritence = true;
    }
    
    public boolean isInheritenceEnabled() {
        return enableInheritence;
    }
    
    public MockRole getParent() {
        return parent;
    }
}
