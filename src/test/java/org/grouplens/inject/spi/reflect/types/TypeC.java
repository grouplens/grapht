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
