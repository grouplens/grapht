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

import org.apache.commons.lang3.SerializationUtils;
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
        MethodProxy proxy = MethodProxy.of(f);
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
        MethodProxy proxy = MethodProxy.of(fld);
        return SerializationUtils.clone(proxy);
    }
}
