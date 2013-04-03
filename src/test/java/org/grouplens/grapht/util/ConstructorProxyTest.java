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

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ConstructorProxyTest {
    private static class TestClass {
        public TestClass() {}
        public TestClass(String foo) {}
        public TestClass(List<String> foos, boolean frob) {}
    }

    @Test
    public void testBasicConstructor() throws NoSuchMethodException, ClassNotFoundException {
        Constructor f = TestClass.class.getDeclaredConstructor();
        ConstructorProxy proxy = ConstructorProxy.of(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeConstructor() throws NoSuchMethodException, ClassNotFoundException, IOException {
        Constructor f = TestClass.class.getDeclaredConstructor();
        ConstructorProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeParamConstructor() throws NoSuchMethodException, ClassNotFoundException, IOException {
        Constructor f = TestClass.class.getDeclaredConstructor(String.class);
        ConstructorProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeTwoArgConstructor() throws NoSuchMethodException, IOException, ClassNotFoundException {
        Constructor f = TestClass.class.getDeclaredConstructor(List.class, boolean.class);
        ConstructorProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    /**
     * Serialize and deserialize a constructor proxy.
     * @param fld The constructor to serialize
     * @return A constructor proxy that is the result of serializing a proxy for {@code fld} and
     * deserializing it.
     */
    private ConstructorProxy roundTrip(Constructor fld) throws IOException, ClassNotFoundException {
        ConstructorProxy proxy = ConstructorProxy.of(fld);
        return SerializationUtils.clone(proxy);
    }
}
