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
package org.grouplens.inject;

import org.grouplens.inject.spi.reflect.types.InterfaceA;
import org.grouplens.inject.spi.reflect.types.InterfaceB;
import org.grouplens.inject.spi.reflect.types.NamedType;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleE;
import org.grouplens.inject.spi.reflect.types.TypeA;
import org.grouplens.inject.spi.reflect.types.TypeB;
import org.grouplens.inject.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Test;

public class InjectorBuilderTest {
    @Test
    public void testInjectorBuilderWithAnnotatedBindings() throws Exception {
        // Test that injector building works as expected
        TypeA a1 = new TypeA();
        InterfaceA a2 = new TypeA();
        TypeB b1 = new TypeB();
        InterfaceB b2 = new TypeB();
        
        InjectorBuilder b = new InjectorBuilder();
        b.bind(TypeA.class).to(a1);
        b.bind(InterfaceA.class).withQualifier(RoleA.class).to(a2);
        b.bind(TypeB.class).to(b1);
        b.bind(InterfaceB.class).withQualifier(RoleE.class).to(b2);
        Injector i = b.build();
        
        TypeC c = i.getInstance(TypeC.class);
        Assert.assertEquals(5, c.getIntValue());
        Assert.assertSame(a1, c.getTypeA());
        Assert.assertSame(a2, c.getInterfaceA());
        Assert.assertSame(b1, c.getTypeB());
        Assert.assertSame(b2, c.getInterfaceB());
        
        // now assert that it memoizes instances and merges graphs properly
        Assert.assertSame(a1, i.getInstance(TypeA.class));
        Assert.assertSame(a2, i.getInstance(RoleA.class, InterfaceA.class));
        Assert.assertSame(b1, i.getInstance(TypeB.class));
        Assert.assertSame(b2, i.getInstance(RoleE.class, InterfaceB.class));
    }
    
    @Test
    public void testInjectorBuilderWithNamedBindings() throws Exception {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(String.class).withName("unused").to("shouldn't see this"); // extra binding to make sure it's skipped
        b.bind(String.class).withName("test1").to("hello world");
        Injector i = b.build();
        
        NamedType c = i.getInstance(NamedType.class);
        Assert.assertEquals("hello world", c.getNamedString());
        Assert.assertEquals("hello world", i.getInstance("test1", String.class));
    }
    
    @Test
    public void testInjectorMissingNamedBinding() throws Exception {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(String.class).withName("unused").to("shouldn't see this"); // extra binding to make sure it's skipped
        Injector i = b.build();
        
        // since we don't have a 'test1' bound, the resolver falls back to the
        // default String() constructor, which injects the empty string
        NamedType c = i.getInstance(NamedType.class);
        Assert.assertEquals("", c.getNamedString());
    }
}
