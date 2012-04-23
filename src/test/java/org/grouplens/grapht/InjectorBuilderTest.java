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
package org.grouplens.grapht;

import javax.inject.Named;

import org.grouplens.grapht.spi.reflect.types.InterfaceA;
import org.grouplens.grapht.spi.reflect.types.InterfaceB;
import org.grouplens.grapht.spi.reflect.types.NamedType;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.spi.reflect.types.TypeA;
import org.grouplens.grapht.spi.reflect.types.TypeB;
import org.grouplens.grapht.spi.reflect.types.TypeC;
import org.grouplens.grapht.util.AnnotationBuilder;
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
        b.bind(InterfaceB.class).withQualifier(RoleD.class).to(b2);
        Injector i = b.build();
        
        TypeC c = i.getInstance(TypeC.class);
        Assert.assertEquals(5, c.getIntValue());
        Assert.assertSame(a1, c.getTypeA());
        Assert.assertSame(a2, c.getInterfaceA());
        Assert.assertSame(b1, c.getTypeB());
        Assert.assertSame(b2, c.getInterfaceB());
        
        // now assert that it memoizes instances and merges graphs properly
        Assert.assertSame(a1, i.getInstance(TypeA.class));
        Assert.assertSame(a2, i.getInstance(new AnnotationBuilder<RoleA>(RoleA.class).build(), InterfaceA.class));
        Assert.assertSame(b1, i.getInstance(TypeB.class));
        Assert.assertSame(b2, i.getInstance(new AnnotationBuilder<RoleD>(RoleD.class).build(), InterfaceB.class));
    }
    
    @Test
    public void testInjectorBuilderWithNamedBindings() throws Exception {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "unused").build()).to("shouldn't see this"); // extra binding to make sure it's skipped
        b.bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test1").build()).to("hello world");
        Injector i = b.build();
        
        NamedType c = i.getInstance(NamedType.class);
        Assert.assertEquals("hello world", c.getNamedString());
        Assert.assertEquals("hello world", i.getInstance(new AnnotationBuilder<Named>(Named.class).set("value", "test1").build(), String.class));
    }
    
    @Test
    public void testInjectorMissingNamedBinding() throws Exception {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "unused").build()).to("shouldn't see this"); // extra binding to make sure it's skipped
        Injector i = b.build();
        
        // since we don't have a 'test1' bound, the resolver falls back to the
        // default String() constructor, which injects the empty string
        NamedType c = i.getInstance(NamedType.class);
        Assert.assertEquals("", c.getNamedString());
    }
}
