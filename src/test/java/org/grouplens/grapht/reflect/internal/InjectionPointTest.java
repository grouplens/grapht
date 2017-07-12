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
package org.grouplens.grapht.reflect.internal;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.annotation.Attribute;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.internal.types.RoleA;
import org.grouplens.grapht.reflect.internal.types.RoleB;
import org.grouplens.grapht.reflect.internal.types.RoleD;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;

public class InjectionPointTest {
    private static <T extends Annotation> Annotation named(String name) {
        return AnnotationBuilder.of(Named.class).setValue(name).build();
    }
    
    @Test
    public void testAttributesLookup() throws Exception {
        Constructor<CtorType> ctor = CtorType.class.getConstructor(Object.class, String.class, Optional.class);
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(ctor, 0);
        ParameterInjectionPoint p2 = new ParameterInjectionPoint(ctor, 1);
        
        // p1 has the transient attribute, p2 does not
        Assert.assertNotNull(p1.getAttribute(Transient.class));
        Assert.assertNull(p2.getAttribute(Transient.class));
        Assert.assertEquals(1, p1.getAttributes().size());
        Assert.assertEquals(0, p2.getAttributes().size());
    }
    
    @Test
    public void testConstructorParameterInjectionPoint() throws Exception {
        // created expected injection points
        Constructor<CtorType> ctor = CtorType.class.getConstructor(Object.class, String.class, Optional.class);
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(ctor, 0);
        ParameterInjectionPoint p2 = new ParameterInjectionPoint(ctor, 1);
        ParameterInjectionPoint p3 = new ParameterInjectionPoint(ctor, 2);

        Set<InjectionPoint> expected = Sets.newHashSet(p1, p2, p3);
        // verify that the qualifiers and types are identified properly
        Assert.assertThat(p1.getQualifier(), instanceOf(RoleA.class));
        Assert.assertThat(p2.getQualifier(), instanceOf(RoleB.class));
        Assert.assertThat(p1.getAttribute(Transient.class), notNullValue());
        Assert.assertThat(p1.getAttributes(), contains(instanceOf(Transient.class)));
        Assert.assertThat(p2.getAttributes(), hasSize(0));
        Assert.assertThat(p2.getAttribute(Transient.class), nullValue());

        Assert.assertEquals(Object.class, p1.getType());
        Assert.assertEquals(String.class, p2.getType());
        
        // verify nullability and transience
        Assert.assertFalse(p1.isOptional());
        Assert.assertTrue(p2.isOptional());
        
        Assert.assertEquals(expected, getInjectionPoints(CtorType.class));
    }

    @Test
    public void testConstructorParameterOptionalDep() throws NoSuchMethodException {
        Constructor<CtorType> ctor = CtorType.class.getConstructor(Object.class, String.class, Optional.class);
        ParameterInjectionPoint point = new ParameterInjectionPoint(ctor, 2);
        Assert.assertThat(point.getQualifier(), instanceOf(RoleB.class));
        Assert.assertThat(point.getAttributes(), hasSize(0));
        Assert.assertThat(point.getAttribute(Transient.class), nullValue());
        Assert.assertEquals(String.class, point.getType());
        Assert.assertTrue(point.isOptional());
    }

    @Test
    public void testSetterMethodInjectionPoint() throws Exception {
        // create expected injection points
        Method m1 = SetterType.class.getMethod("setA", Object.class);
        Method m2 = SetterType.class.getMethod("setB", String.class);
        Method m3 = SetterType.class.getMethod("setMulti", Object.class, String.class);
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(m1, 0);
        ParameterInjectionPoint p2 = new ParameterInjectionPoint(m2, 0);
        ParameterInjectionPoint p3 = new ParameterInjectionPoint(m3, 0);
        ParameterInjectionPoint p4 = new ParameterInjectionPoint(m3, 1);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        expected.add(p3);
        expected.add(p4);
        
        // verify that the qualifiers, types, attrs are identified properly
        Assert.assertThat(p1.getQualifier(), instanceOf(RoleA.class));
        Assert.assertThat(p1.getAttribute(Transient.class), notNullValue());
        Assert.assertThat(p1.getAttributes(), contains(instanceOf(Transient.class)));

        Assert.assertThat(p2.getQualifier(), instanceOf(RoleB.class));
        Assert.assertThat(p2.getAttributes(), hasSize(0));
        Assert.assertThat(p2.getAttribute(Transient.class), nullValue());

        Assert.assertThat(p3.getQualifier(), nullValue());
        Assert.assertThat(p3.getAttributes(), hasSize(0));

        Assert.assertThat(p4.getQualifier(), instanceOf(RoleD.class));
        Assert.assertThat(p4.getAttributes(), hasSize(0));
        Assert.assertThat(p4.getAttribute(Transient.class), nullValue());

        Assert.assertEquals(Object.class, p1.getType());
        Assert.assertEquals(String.class, p2.getType());
        Assert.assertEquals(Object.class, p3.getType());
        Assert.assertEquals(String.class, p4.getType());
        
        // verify nullability and transience
        Assert.assertFalse(p1.isOptional());
        Assert.assertFalse(p2.isOptional());
        Assert.assertFalse(p3.isOptional());
        Assert.assertTrue(p4.isOptional());
        
        Assert.assertEquals(expected, getInjectionPoints(SetterType.class));
    }
    
    @Test
    public void testPrimitiveBoxing() throws Exception {
        Method m1 = PrimitiveType.class.getMethod("setUnboxed", int.class);
        Method m2 = PrimitiveType.class.getMethod("setBoxed", Integer.class);
        
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(m1, 0);
        ParameterInjectionPoint p2 = new ParameterInjectionPoint(m2, 0);
        
        // make sure that both injection points are normalized to boxed types
        Assert.assertEquals(Integer.class, p1.getType());
        Assert.assertEquals(Integer.class, p2.getType());
    }
    
    @Test
    public void testNamedQualifiers() throws Exception {
        Constructor<NamedType> ctor = NamedType.class.getConstructor(String.class, Integer.class);
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(ctor, 0);
        ParameterInjectionPoint p2 = new ParameterInjectionPoint(ctor, 1);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        
        // verify that the qualifiers and types are identified properly
        Assert.assertThat(p1.getQualifier(), equalTo(named("test1")));
        Assert.assertThat(p2.getQualifier(), equalTo(named("test2")));

        Assert.assertEquals(String.class, p1.getType());
        Assert.assertEquals(Integer.class, p2.getType());
        
        Assert.assertEquals(expected, getInjectionPoints(NamedType.class));
    }
    
    @Test
    public void testFieldInjectionPoints() throws Exception {
        Field f1 = FieldType.class.getField("field");
        FieldInjectionPoint p1 = new FieldInjectionPoint(f1);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        
        Assert.assertEquals(expected, getInjectionPoints(FieldType.class));
    }
    
    @Test
    public void testCombinedDesires() throws Exception {
        // create expected injection points
        Constructor<AllTypes> ctor = AllTypes.class.getConstructor(Object.class, String.class);
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(ctor, 0);
        ParameterInjectionPoint p2 = new ParameterInjectionPoint(ctor, 1);
        Method m1 = AllTypes.class.getMethod("setC", Object.class);
        Method m2 = AllTypes.class.getMethod("setD", String.class);
        Field f1 = AllTypes.class.getField("field");
        FieldInjectionPoint p3 = new FieldInjectionPoint(f1);
        ParameterInjectionPoint p4 = new ParameterInjectionPoint(m1, 0);
        ParameterInjectionPoint p5 = new ParameterInjectionPoint(m2, 0);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        expected.add(p2);
        expected.add(p3);
        expected.add(p4);
        expected.add(p5);
        
        Assert.assertEquals(expected, getInjectionPoints(AllTypes.class));
    }
    
    @Test
    public void testSubclassOverrides() throws Exception {
        Method m1 = SubType.class.getMethod("injectMethod", Object.class);
        ParameterInjectionPoint p1 = new ParameterInjectionPoint(m1, 0);
        
        Set<InjectionPoint> expected = new HashSet<InjectionPoint>();
        expected.add(p1);
        
        Assert.assertEquals(expected, getInjectionPoints(SubType.class));
    }
    
    private Set<InjectionPoint> getInjectionPoints(Class<?> types) {
        List<Desire> desires = ReflectionDesire.getDesires(types);
        Set<InjectionPoint> points = new HashSet<InjectionPoint>();
        for (Desire rd: desires) {
            points.add(rd.getInjectionPoint());
        }
        return points;
    }
    
    @Attribute
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Transient { }
    
    public static class CtorType {
        @Inject
        public CtorType(@Transient @RoleA Object a, @Nullable @RoleB String b,
                        @RoleB Optional<String> c) { }
        
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
    
    public static class FieldType {
        @Inject public String field;
    }
    
    public static class AllTypes {
        @Inject public String field;
        
        @Inject
        public AllTypes(@RoleA Object a, @RoleB String b) { }
        
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
    
    public static class SuperType {
        @Inject
        public void nonInjectMethod(Object o) { }
        
        public void injectMethod(Object o) { }
    }
    
    public static class SubType extends SuperType {
        public void nonInjectMethod(Object o) { }
        
        @Inject
        public void injectMethod(Object o) { }
    }
}
