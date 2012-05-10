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
package org.grouplens.grapht.spi.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;

import org.grouplens.grapht.annotation.Attribute;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.spi.reflect.types.RoleB;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.util.AnnotationBuilder;
import org.junit.Assert;
import org.junit.Test;

public class InjectionPointTest {
    private static <T extends Annotation> Attributes qualifier(Class<T> qtype, boolean hasAttr) {
        if (hasAttr) {
            return new AttributesImpl(AnnotationBuilder.of(qtype).build(), AnnotationBuilder.of(Transient.class).build());
        } else {
            return new AttributesImpl(AnnotationBuilder.of(qtype).build());
        }
    }
    
    private static <T extends Annotation> Attributes named(String name, boolean hasAttr) {
        if (hasAttr) {
            return new AttributesImpl(AnnotationBuilder.of(Named.class).setValue(name).build(), AnnotationBuilder.of(Transient.class).build());
        } else {
            return new AttributesImpl(AnnotationBuilder.of(Named.class).setValue(name).build());
        }
    }
    
    @Test
    public void testAttributesLookup() throws Exception {
        Constructor<CtorType> ctor = CtorType.class.getConstructor(Object.class, String.class);
        ConstructorParameterInjectionPoint p1 = new ConstructorParameterInjectionPoint(ctor, 0);
        ConstructorParameterInjectionPoint p2 = new ConstructorParameterInjectionPoint(ctor, 1);
        
        // p1 has the transient attribute, p2 does not
        Assert.assertNotNull(p1.getAttributes().getAttribute(Transient.class));
        Assert.assertNull(p2.getAttributes().getAttribute(Transient.class));
        Assert.assertEquals(1, p1.getAttributes().getAttributes().size());
        Assert.assertEquals(0, p2.getAttributes().getAttributes().size());
    }
    
    @Test
    public void testConstructorParameterInjectionPoint() throws Exception {
        // created expected injection points
        Constructor<CtorType> ctor = CtorType.class.getConstructor(Object.class, String.class);
        ConstructorParameterInjectionPoint p1 = new ConstructorParameterInjectionPoint(ctor, 0);
        ConstructorParameterInjectionPoint p2 = new ConstructorParameterInjectionPoint(ctor, 1);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        
        // verify that the qualifiers and types are identified properly
        Assert.assertEquals(qualifier(RoleA.class, true), p1.getAttributes());
        Assert.assertEquals(qualifier(RoleB.class, false), p2.getAttributes());
        Assert.assertEquals(Object.class, p1.getType());
        Assert.assertEquals(String.class, p2.getType());
        
        // verify nullability and transience
        Assert.assertFalse(p1.isNullable());
        Assert.assertTrue(p2.isNullable());
        
        Assert.assertEquals(expected, getInjectionPoints(CtorType.class));
    }
    
    @Test
    public void testSetterMethodInjectionPoint() throws Exception {
        // create expected injection points
        Method m1 = SetterType.class.getMethod("setA", Object.class);
        Method m2 = SetterType.class.getMethod("setB", String.class);
        Method m3 = SetterType.class.getMethod("setMulti", Object.class, String.class);
        SetterInjectionPoint p1 = new SetterInjectionPoint(m1, 0);
        SetterInjectionPoint p2 = new SetterInjectionPoint(m2, 0);
        SetterInjectionPoint p3 = new SetterInjectionPoint(m3, 0);
        SetterInjectionPoint p4 = new SetterInjectionPoint(m3, 1);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        expected.add(p3);
        expected.add(p4);
        
        // verify that the qualifiers, types, attrs are identified properly
        Assert.assertEquals(qualifier(RoleA.class, true), p1.getAttributes());
        Assert.assertEquals(qualifier(RoleB.class, false), p2.getAttributes());
        Assert.assertEquals(new AttributesImpl(), p3.getAttributes());
        Assert.assertEquals(qualifier(RoleD.class, false), p4.getAttributes());
        Assert.assertEquals(Object.class, p1.getType());
        Assert.assertEquals(String.class, p2.getType());
        Assert.assertEquals(Object.class, p3.getType());
        Assert.assertEquals(String.class, p4.getType());
        
        // verify nullability and transience
        Assert.assertFalse(p1.isNullable());
        Assert.assertFalse(p2.isNullable());
        Assert.assertFalse(p3.isNullable());
        Assert.assertTrue(p4.isNullable());
        
        Assert.assertEquals(expected, getInjectionPoints(SetterType.class));
    }
    
    @Test
    public void testPrimitiveBoxing() throws Exception {
        Method m1 = PrimitiveType.class.getMethod("setUnboxed", int.class);
        Method m2 = PrimitiveType.class.getMethod("setBoxed", Integer.class);
        
        SetterInjectionPoint p1 = new SetterInjectionPoint(m1, 0);
        SetterInjectionPoint p2 = new SetterInjectionPoint(m2, 0);
        
        // make sure that both injection points are normalized to boxed types
        Assert.assertEquals(Integer.class, p1.getType());
        Assert.assertEquals(Integer.class, p2.getType());
    }
    
    @Test
    public void testNamedQualifiers() throws Exception {
        Constructor<NamedType> ctor = NamedType.class.getConstructor(String.class, Integer.class);
        ConstructorParameterInjectionPoint p1 = new ConstructorParameterInjectionPoint(ctor, 0);
        ConstructorParameterInjectionPoint p2 = new ConstructorParameterInjectionPoint(ctor, 1);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        
        // verify that the qualifiers and types are identified properly
        Assert.assertEquals(named("test1", false), p1.getAttributes());
        Assert.assertEquals(named("test2", false), p2.getAttributes());
        Assert.assertEquals(String.class, p1.getType());
        Assert.assertEquals(Integer.class, p2.getType());
        
        Assert.assertEquals(expected, getInjectionPoints(NamedType.class));
    }
    
    @Test
    public void testCombinedDesires() throws Exception {
        // create expected injection points
        Constructor<BothTypes> ctor = BothTypes.class.getConstructor(Object.class, String.class);
        ConstructorParameterInjectionPoint p1 = new ConstructorParameterInjectionPoint(ctor, 0);
        ConstructorParameterInjectionPoint p2 = new ConstructorParameterInjectionPoint(ctor, 1);
        Method m1 = BothTypes.class.getMethod("setC", Object.class);
        Method m2 = BothTypes.class.getMethod("setD", String.class);
        SetterInjectionPoint p3 = new SetterInjectionPoint(m1, 0);
        SetterInjectionPoint p4 = new SetterInjectionPoint(m2, 0);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        expected.add(p3);
        expected.add(p4);
        
        Assert.assertEquals(expected, getInjectionPoints(BothTypes.class));
    }
    
    private Set<InjectionPoint> getInjectionPoints(Class<?> types) {
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(types);
        Set<InjectionPoint> points = new HashSet<InjectionPoint>();
        for (ReflectionDesire rd: desires) {
            points.add(rd.getInjectionPoint());
        }
        return points;
    }
    
    @Attribute
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Transient { }
    
    public static class CtorType {
        @Inject
        public CtorType(@Transient @RoleA Object a, @Nullable @RoleB String b) { }
        
        // other constructor to be ignored
        public CtorType() { }
    }
    
    public static class SetterType {
        @Inject
        public void setA(@Transient @RoleA Object a) { }
        
        @Inject
        public void setB(@RoleB String b) { }
        
        @Inject
        public boolean setMulti(Object a, @Nullable @RoleD String c) {
            return false;
        }
        
        // other setter to be ignored
        public void setX(Object c) { }
    }
    
    public static class BothTypes {
        @Inject
        public BothTypes(@RoleA Object a, @RoleB String b) { }
        
        @Inject
        public void setC(Object c) { }
        
        @Inject
        public void setD(@RoleD String d) { }
    }
    
    public static class PrimitiveType {
        @Inject
        public void setUnboxed(int a) { }
        
        @Inject
        public void setBoxed(Integer a) { }
    }
    
    public static class NamedType {
        @Inject
        public NamedType(@Named("test1") String a, @Named("test2") Integer b) { }
    }
}
