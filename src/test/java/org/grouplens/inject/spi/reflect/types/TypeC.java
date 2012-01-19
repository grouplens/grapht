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
package org.grouplens.inject.spi.reflect.types;

import javax.inject.Inject;

public class TypeC {
    private final int value;
    private InterfaceA a1;
    private TypeA a2;
    private InterfaceB b1;
    private TypeB b2;
    
    @Inject
    public TypeC(@ParameterA int intValue) {
        // ParameterA has a default value of 5
        value = intValue;
    }
    
    @Inject
    public void setRoleA(@RoleA InterfaceA a) {
        // RoleA has no default type, InterfaceA is implemented by TypeA,
        // which is then provided by a ProviderA
        a1 = a;
    }
    
    @Inject
    public void setTypeA(TypeA a) {
        // TypeA is provided by ProviderA, which creates instances of TypeB
        a2 = a;
    }
    
    @Inject
    public void setRoleE(@RoleE InterfaceB b) {
        // RoleE has no default type, but inherits from RoleD, which defaults to TypeB
        b1 = b;
    }
    
    @Inject
    public void setTypeB(TypeB b) {
        // No default desire, but TypeB is satisfiable on its own
        b2 = b;
    }
    
    public TypeB getTypeB() {
        return b2;
    }
    
    public InterfaceB getInterfaceB() {
        return b1;
    }
    
    public int getIntValue() {
        return value;
    }
    
    public InterfaceA getInterfaceA() {
        return a1;
    }
    
    public TypeA getTypeA() {
        return a2;
    }
}
