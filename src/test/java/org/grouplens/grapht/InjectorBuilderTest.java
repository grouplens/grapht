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

import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.reflect.internal.types.*;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Named;

public class InjectorBuilderTest {
    @Test
    public void testNewInstanceProviderCachePolicy() throws Exception {
        // Test that injecting a new-instance provider creates
        // new instances each time
        InjectorBuilder b = new InjectorBuilder().setProviderInjectionEnabled(true);
        b.bind(CycleA.class).unshared().to(CycleA.class);
        b.bind(CycleB.class).shared().to(CycleB.class);
        Injector i = b.build();
        
        CycleB cycleB = i.getInstance(CycleB.class);
        
        Assert.assertNotSame(cycleB.pa.get(), cycleB.pa.get());
    }
    
    @Test
    public void testMemoizedProviderCachePolicy() throws Exception {
        // Test that injecting a memoized provider into a new instance
        // reuses the same instance
        InjectorBuilder b = new InjectorBuilder().setProviderInjectionEnabled(true);
        b.bind(CycleA.class).shared().to(CycleA.class);
        b.bind(CycleB.class).unshared().to(CycleB.class);
        Injector i = b.build();
        
        CycleB b1 = i.getInstance(CycleB.class);
        CycleB b2 = i.getInstance(CycleB.class);
        
        Assert.assertNotSame(b1, b2);
        Assert.assertSame(b1.pa, b2.pa);
        Assert.assertSame(b1.pa.get(), b2.pa.get());
    }
    
    @Test
    public void testProviderInjectionCycleBreaking() throws Exception {
        InjectorBuilder b = new InjectorBuilder().setProviderInjectionEnabled(true);
        Injector i = b.build();
        
        CycleA cycleA = i.getInstance(CycleA.class);
        Assert.assertNotNull(cycleA.b);
        Assert.assertSame(cycleA, cycleA.b.pa.get());
    }
    
    @Test
    public void testSimpleProviderInjection() throws Exception {
        InjectorBuilder b = new InjectorBuilder().setProviderInjectionEnabled(true);
        Injector i = b.build();
        
        TypeD d = i.getInstance(TypeD.class);
        Assert.assertNotNull(d.getProvider());
        
        // assert that default configuration was used
        TypeC c = d.getProvider().get();
        Assert.assertEquals(5, c.getIntValue());
        Assert.assertTrue(c.getInterfaceA() instanceof TypeB);
        Assert.assertTrue(c.getTypeA() instanceof TypeB);
        Assert.assertTrue(c.getInterfaceB() instanceof TypeB);
        Assert.assertTrue(c.getTypeB() instanceof TypeB);
    }
    
    @Test
    public void testNewInstanceCachePolicy() throws Exception {
        // Test that setting the cache policy to NEW_INSTANCE 
        // overrides default MEMOIZE behavior
        InjectorBuilder b = new InjectorBuilder();
        b.bind(InterfaceA.class).unshared().to(TypeA.class);
        b.bind(InterfaceB.class).to(TypeB.class);
        Injector i = b.build();
        
        InterfaceA a1 = i.getInstance(InterfaceA.class);
        InterfaceA a2 = i.getInstance(InterfaceA.class);
        InterfaceB b1 = i.getInstance(InterfaceB.class);
        InterfaceB b2 = i.getInstance(InterfaceB.class);
        
        Assert.assertTrue(a1 instanceof TypeA);
        Assert.assertTrue(a2 instanceof TypeA);
        Assert.assertNotSame(a1, a2);
        
        Assert.assertTrue(b1 instanceof TypeB);
        Assert.assertTrue(b2 instanceof TypeB);
        Assert.assertSame(b1, b2);
    }
    
    @Test
    public void testMemoizeCachePolicy() throws Exception {
        // Test that setting the cache policy to MEMOIZE
        // overrides the default NEW_INSTANCE behavior
        InjectorBuilder b = new InjectorBuilder();
        b.setDefaultCachePolicy(CachePolicy.NEW_INSTANCE);
        b.bind(InterfaceA.class).shared().to(TypeA.class);
        b.bind(InterfaceB.class).to(TypeB.class);
        Injector i = b.build();
        
        InterfaceA a1 = i.getInstance(InterfaceA.class);
        InterfaceA a2 = i.getInstance(InterfaceA.class);
        InterfaceB b1 = i.getInstance(InterfaceB.class);
        InterfaceB b2 = i.getInstance(InterfaceB.class);
        
        Assert.assertTrue(a1 instanceof TypeA);
        Assert.assertTrue(a2 instanceof TypeA);
        Assert.assertSame(a1, a2);
        
        Assert.assertTrue(b1 instanceof TypeB);
        Assert.assertTrue(b2 instanceof TypeB);
        Assert.assertNotSame(b1, b2);
    }
    
    @Test
    public void testMemoizeDefaultCachePolicy() throws Exception {
        // Test that using the default binding cache policy 
        // correctly uses the default MEMOIZE policy of the injector
        InjectorBuilder b = new InjectorBuilder();
        b.setDefaultCachePolicy(CachePolicy.MEMOIZE);
        b.bind(InterfaceA.class).to(TypeA.class);
        b.bind(InterfaceB.class).to(TypeB.class);
        Injector i = b.build();
        
        InterfaceA a1 = i.getInstance(InterfaceA.class);
        InterfaceA a2 = i.getInstance(InterfaceA.class);
        InterfaceB b1 = i.getInstance(InterfaceB.class);
        InterfaceB b2 = i.getInstance(InterfaceB.class);
        
        Assert.assertTrue(a1 instanceof TypeA);
        Assert.assertTrue(a2 instanceof TypeA);
        Assert.assertSame(a1, a2);
        
        Assert.assertTrue(b1 instanceof TypeB);
        Assert.assertTrue(b2 instanceof TypeB);
        Assert.assertSame(b1, b2);
    }
    
    @Test
    public void testNewInstanceDefaultCachePolicy() throws Exception {
        // Test that using the default binding cache policy 
        // correctly uses the default MEMOIZE policy of the injector
        InjectorBuilder b = new InjectorBuilder();
        b.setDefaultCachePolicy(CachePolicy.NEW_INSTANCE);
        b.bind(InterfaceA.class).to(TypeA.class);
        b.bind(InterfaceB.class).to(TypeB.class);
        Injector i = b.build();
        
        InterfaceA a1 = i.getInstance(InterfaceA.class);
        InterfaceA a2 = i.getInstance(InterfaceA.class);
        InterfaceB b1 = i.getInstance(InterfaceB.class);
        InterfaceB b2 = i.getInstance(InterfaceB.class);
        
        Assert.assertTrue(a1 instanceof TypeA);
        Assert.assertTrue(a2 instanceof TypeA);
        Assert.assertNotSame(a1, a2);
        
        Assert.assertTrue(b1 instanceof TypeB);
        Assert.assertTrue(b2 instanceof TypeB);
        Assert.assertNotSame(b1, b2);        
    }
    
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
    
    @Test(expected=InjectionException.class)
    public void testInjectorMissingNamedBinding() throws Exception {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "unused").build()).to("shouldn't see this"); // extra binding to make sure it's skipped
        Injector i = b.build();
        
        // since we don't have a 'test1' bound, the resolver cannot find 
        // a usable constructor for String (although it has the default constructor,
        // it defines others that are not injectable).
        i.getInstance(NamedType.class);
    }
    
    @Test(expected=InjectionException.class)
    public void testInjectorNoConstructor() throws Exception {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(ShouldWork.class).to(NotInjectable.class);
        Injector i = b.build();
        
        i.getInstance(ShouldWork.class);
    }

    @Test
    public void testNullBinding() throws InjectionException {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(InterfaceA.class).toNull();
        Injector i = b.build();
        TypeN n = i.getInstance(TypeN.class);
        Assert.assertNotNull(n);
        Assert.assertNull(n.getObject());
    }

    @Test(expected=InjectionException.class)
    public void testBadNullBinding() throws InjectionException {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(InterfaceA.class).toNull();
        Injector i = b.build();
        i.getInstance(TypeN2.class);
    }
    
    public static interface ShouldWork { }
    
    public static class NotInjectable implements ShouldWork {
        public NotInjectable(Object o) { }
    }
}
