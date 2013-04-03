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

import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class MethodProxyTest {
    private static class TestClass {
        String foo(String name) { return "Hello, " + name; }
        public int bar() { return -1; }
        public void wombat(List<String> foo, String... args) {
            Collections.addAll(foo, args);
        }
    }

    @Test
    public void testBasicMethod() throws NoSuchMethodException, ClassNotFoundException {
        Method f = TestClass.class.getDeclaredMethod("foo", String.class);
        MethodProxy proxy = MethodProxy.forMethod(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeMethod() throws NoSuchMethodException, ClassNotFoundException, IOException {
        Method f = TestClass.class.getDeclaredMethod("foo", String.class);
        MethodProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeNullaryMethod() throws NoSuchMethodException, ClassNotFoundException, IOException {
        Method f = TestClass.class.getDeclaredMethod("bar");
        MethodProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeVariadicMethod() throws NoSuchMethodException, IOException, ClassNotFoundException {
        Method f = TestClass.class.getDeclaredMethod("wombat", List.class, String[].class);
        MethodProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    /**
     * Serialize and deserialize a method proxy.
     * @param fld The method to serialize
     * @return A method proxy that is the result of serializing a proxy for {@code fld} and
     * deserializing it.
     */
    private MethodProxy roundTrip(Method fld) throws IOException, ClassNotFoundException {
        MethodProxy proxy = MethodProxy.forMethod(fld);
        return TestUtils.serializeRoundTrip(proxy);
    }
}
