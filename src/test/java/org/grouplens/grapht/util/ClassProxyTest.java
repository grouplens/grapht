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
