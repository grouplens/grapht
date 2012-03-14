package org.grouplens.inject;

import org.grouplens.inject.spi.reflect.types.InterfaceA;
import org.grouplens.inject.spi.reflect.types.InterfaceB;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleE;
import org.grouplens.inject.spi.reflect.types.TypeA;
import org.grouplens.inject.spi.reflect.types.TypeB;
import org.grouplens.inject.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Test;

public class InjectorBuilderTest {
    @Test
    public void testInjectorBuilder() throws Exception {
        // Test that injector building works as expected
        TypeA a1 = new TypeA();
        InterfaceA a2 = new TypeA();
        TypeB b1 = new TypeB();
        InterfaceB b2 = new TypeB();
        
        InjectorBuilder b = new InjectorBuilder();
        b.bind(TypeA.class).to(a1);
        b.bind(InterfaceA.class).withRole(RoleA.class).to(a2);
        b.bind(TypeB.class).to(b1);
        b.bind(InterfaceB.class).withRole(RoleE.class).to(b2);
        
        TypeC c = b.build().getInstance(TypeC.class);
        Assert.assertEquals(5, c.getIntValue());
        Assert.assertSame(a1, c.getTypeA());
        Assert.assertSame(a2, c.getInterfaceA());
        Assert.assertSame(b1, c.getTypeB());
        Assert.assertSame(b2, c.getInterfaceB());
    }
}
