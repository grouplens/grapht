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
package org.grouplens.grapht.util;

import org.grouplens.grapht.reflect.internal.types.InterfaceA;
import org.grouplens.grapht.reflect.internal.types.InterfaceB;
import org.grouplens.grapht.reflect.internal.types.TypeA;
import org.grouplens.grapht.reflect.internal.types.TypeB;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import static org.junit.Assert.fail;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import javax.inject.Provider;

@SuppressWarnings("rawtypes")
public class TypesTest {
    @Test
    public void testEraseClass() {
        assertThat(Types.erase(String.class),
                equalTo((Class) String.class));
    }

    @Test
    public void testEraseParamType() {
        assertThat(Types.erase(ArrayList.class.getGenericSuperclass()), equalTo((Class) AbstractList.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEraseVariable() {
        Type var = List.class.getTypeParameters()[0];
        Types.erase(var);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEraseWildcard() throws NoSuchFieldException {
        class WildTest {
            @SuppressWarnings("unused")
            public List<? extends InputStream> field;
        }

        Field field = WildTest.class.getField("field");
        Type ft = field.getGenericType();
        assertThat(Types.erase(ft), equalTo((Class) List.class));
        ParameterizedType pft = (ParameterizedType) ft;
        Type param = pft.getActualTypeArguments()[0];
        assertThat(param, CoreMatchers.instanceOf(WildcardType.class));
        // finally, throw an illegal argument exception
        Types.erase(param);
    }

    private void testProvidedType(Class<? extends Provider<?>> cls){
        Class<?> result = Types.getProvidedType(TestingProviders.SimpleProvider.class);
        assertThat(result, equalTo((Class) TestingProviders.Target.class));
    }

    @Test
    public void testSimpleProvider(){
        testProvidedType(TestingProviders.SimpleProvider.class);
    }

    @Test
    public void testAbstractProvider(){
        testProvidedType(TestingProviders.SubtypedProvider.class);
        testProvidedType(TestingProviders.SubtypedProvider2.class);
    }

    @Test
    public void testInterfaceProvider(){
        testProvidedType(TestingProviders.ImplementedProvider.class);
        testProvidedType(TestingProviders.ImplementedProvider2.class);
    }

    @Test
    public void testBoundedProvider(){
        Class<?> cls = new TestingProviders.BoundedProvider<TestingProviders.Target>().getClass();
        try{
            Types.getProvidedType((Class<? extends Provider<?>>) cls);
            fail("getProvidedType didn't throw an IllegalArgumentException");
        }
        catch (IllegalArgumentException e){
            // This is the correct behavior
        }
        cls = new TestingProviders.UnboundedProvider<TestingProviders.Target>().getClass();
        try{
            Types.getProvidedType((Class<? extends Provider<?>>) cls);
            fail("getProvidedType didn't throw an IllegalArgumentException");
        }
        catch (IllegalArgumentException e){
            // This is the correct behavior
        }
    }

    @Test
    public void testMultiBoundProvider(){
        Class<?> cls = new TestingProviders.MultiBoundProvider<TestingProviders.Target>().getClass();
        try{
            Types.getProvidedType((Class<? extends Provider<?>>) cls);
            fail("getProvidedType didn't throw an IllegalArgumentException");
        }
        catch (IllegalArgumentException e){
            // This is the correct behavior
        }
    }

    @Test
    public void testSameClassDistance() {
        assertThat(Types.getTypeDistance(String.class, String.class),
                   equalTo(0));
    }

    @Test
    public void testSubClassDistance() {
        assertThat(Types.getTypeDistance(TypeB.class, TypeA.class),
                   equalTo(1));
    }

    @Test
    public void testInterfaceDistance() {
        assertThat(Types.getTypeDistance(TypeA.class, InterfaceA.class),
                   equalTo(1));
    }

    @Test
    public void testTwoStepInterfaceDistance() {
        assertThat(Types.getTypeDistance(TypeB.class, InterfaceA.class),
                   equalTo(2));
    }

    @Test
    public void testSecondInterfaceDistance() {
        assertThat(Types.getTypeDistance(TypeB.class, InterfaceB.class),
                   equalTo(1));
    }

    @Test
    public void testBadTypeDistance() {
        try {
            Types.getTypeDistance(TypeB.class, String.class);
            fail("type distance on bad type should throw exception");
        } catch (IllegalArgumentException e) {
            /* expected */
        }
    }

    public static class Inner {}
}
