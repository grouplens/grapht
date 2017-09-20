/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect.internal.types;

import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.internal.ParameterInjectionPoint;

import javax.inject.Inject;

public class TypeC {
    public static final InjectionPoint CONSTRUCTOR;
    public static final InjectionPoint INTERFACE_A;
    public static final InjectionPoint TYPE_A;
    public static final InjectionPoint INTERFACE_B;
    public static final InjectionPoint TYPE_B;
    
    static {
        try {
            CONSTRUCTOR = new ParameterInjectionPoint(TypeC.class.getConstructor(int.class), 0);
            INTERFACE_A = new ParameterInjectionPoint(TypeC.class.getMethod("setRoleA", InterfaceA.class), 0);
            TYPE_A = new ParameterInjectionPoint(TypeC.class.getMethod("setTypeA", TypeA.class), 0);
            INTERFACE_B = new ParameterInjectionPoint(TypeC.class.getMethod("setRoleD", InterfaceB.class), 0);
            TYPE_B = new ParameterInjectionPoint(TypeC.class.getMethod("setTypeB", TypeB.class), 0);
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
