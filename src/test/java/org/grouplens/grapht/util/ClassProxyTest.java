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
import java.lang.reflect.Array;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings("rawtypes")
public class ClassProxyTest {
    @Test
    public void testBasicProxy() throws ClassNotFoundException {
        ClassProxy proxy = ClassProxy.of(String.class);
        assertThat(proxy.getClassName(), equalTo("java.lang.String"));
        assertThat(proxy.resolve(), equalTo((Class) String.class));
    }

    @Test
    public void testPrimitiveProxy() throws ClassNotFoundException {
        ClassProxy proxy = ClassProxy.of(int.class);
        assertThat(proxy.getClassName(), equalTo("int"));
        assertThat(proxy.resolve(), equalTo((Class) int.class));
    }

    @Test
    public void testArrayProxy() throws ClassNotFoundException {
        ClassProxy proxy = ClassProxy.of(String[].class);
        assertThat(proxy.getClassName(), equalTo("[Ljava.lang.String;"));
        assertThat(proxy.resolve(),
                   equalTo((Class) Array.newInstance(String.class, 0).getClass()));
    }

    /**
     * Serialize and deserialize a class proxy.
     * @param cls The class to serialize
     * @return A class proxy that is the result of serializing a proxy for {@code cls} and
     * deserializing it.
     */
    private ClassProxy roundTrip(Class<?> cls) throws IOException, ClassNotFoundException {
        ClassProxy proxy = ClassProxy.of(cls);
        return SerializationUtils.clone(proxy);
    }

    @Test
    public void testSerializeString() throws ClassNotFoundException, IOException {
        ClassProxy proxy = roundTrip(String.class);
        assertThat(proxy.resolve(),
                   equalTo((Class) String.class));
    }

    @Test
    public void testSerializePrimitive() throws ClassNotFoundException, IOException {
        ClassProxy proxy = roundTrip(double.class);
        assertThat(proxy.resolve(),
                   equalTo((Class) double.class));
    }

    @Test
    public void testSerializeArray() throws ClassNotFoundException, IOException {
        ClassProxy proxy = roundTrip(String[][].class);
        assertThat(proxy.resolve(),
                   equalTo((Class) String[][].class));
    }

    @Test
    public void testSerializePrimitiveArray() throws ClassNotFoundException, IOException {
        ClassProxy proxy = roundTrip(double[][].class);
        assertThat(proxy.resolve(),
                   equalTo((Class) double[][].class));
    }

    @Test
    public void testEquals() {
        ClassProxy proxy = ClassProxy.of(String.class);
        assertThat(proxy.equals(null), equalTo(false));
        assertThat(proxy.equals(proxy), equalTo(true));
        ClassProxy equal = ClassProxy.of(String.class);
        ClassProxy unequal = ClassProxy.of(List.class);
        assertThat(proxy.equals(equal), equalTo(true));
        assertThat(proxy.equals(unequal), equalTo(false));

        ClassProxy serialized = SerializationUtils.clone(proxy);
        assertThat(proxy.equals(serialized), equalTo(true));

        // and test the hash code
        assertThat(equal.hashCode(), equalTo(proxy.hashCode()));
    }
}
