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
package org.grouplens.grapht.util;

import org.grouplens.grapht.reflect.internal.types.InterfaceA;
import org.grouplens.grapht.reflect.internal.types.InterfaceB;
import org.grouplens.grapht.reflect.internal.types.TypeA;
import org.grouplens.grapht.reflect.internal.types.TypeB;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import javax.inject.Provider;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
    public void testTypedProvider() {
        Provider<String> string = Providers.of("string");
        assertThat(Types.getProvidedType(string),
                   equalTo((Class) String.class));
    }

    /**
     * Test that the most specific type is retrieved when the inferred type is more specific
     * than the observed type.
     */
    @Test
    public void testProvidedTypeCheckInferred() {
        Provider<String> string = new NastyStringProvider("foo");
        assertThat(Types.getProvidedType(string),
                   equalTo((Class) String.class));
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

    private static class UntypedInstProv<T> implements Provider<T> {
        private final T instance;

        public UntypedInstProv(T inst){
            instance = inst;
        }

        @Override
        public T get() {
            return instance;
        }
    }

    private static class NastyStringProvider extends UntypedInstProv<String> {
        public NastyStringProvider(String foo) {
            super(foo);
        }
    }
}
