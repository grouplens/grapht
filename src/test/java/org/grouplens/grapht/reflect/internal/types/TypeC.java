/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht.reflect.internal.types;

import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.internal.ConstructorParameterInjectionPoint;
import org.grouplens.grapht.reflect.internal.SetterInjectionPoint;

import javax.inject.Inject;

public class TypeC {
    public static final InjectionPoint CONSTRUCTOR;
    public static final InjectionPoint INTERFACE_A;
    public static final InjectionPoint TYPE_A;
    public static final InjectionPoint INTERFACE_B;
    public static final InjectionPoint TYPE_B;
    
    static {
        try {
            CONSTRUCTOR = new ConstructorParameterInjectionPoint(TypeC.class.getConstructor(int.class), 0);
            INTERFACE_A = new SetterInjectionPoint(TypeC.class.getMethod("setRoleA", InterfaceA.class), 0);
            TYPE_A = new SetterInjectionPoint(TypeC.class.getMethod("setTypeA", TypeA.class), 0);
            INTERFACE_B = new SetterInjectionPoint(TypeC.class.getMethod("setRoleD", InterfaceB.class), 0);
            TYPE_B = new SetterInjectionPoint(TypeC.class.getMethod("setTypeB", TypeB.class), 0);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
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
    public void setRoleD(@RoleD InterfaceB b) {
        // RoleD defaults to TypeB
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
