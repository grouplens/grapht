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

import java.io.*;
import java.lang.reflect.Field;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class FieldProxyTest {
    private static class TestClass {
        String foo;
        int bar;
    }

    @Test
    public void testBasicField() throws NoSuchFieldException, ClassNotFoundException {
        Field f = TestClass.class.getDeclaredField("foo");
        FieldProxy proxy = FieldProxy.of(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeField() throws NoSuchFieldException, ClassNotFoundException, IOException {
        Field f = TestClass.class.getDeclaredField("foo");
        FieldProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializePrimitiveField() throws NoSuchFieldException, ClassNotFoundException, IOException {
        Field f = TestClass.class.getDeclaredField("bar");
        FieldProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    /**
     * Serialize and deserialize a field proxy.
     * @param fld The field to serialize
     * @return A field proxy that is the result of serializing a proxy for {@code fld} and
     * deserializing it.
     */
    private FieldProxy roundTrip(Field fld) throws IOException, ClassNotFoundException {
        FieldProxy proxy = FieldProxy.of(fld);
        return SerializationUtils.clone(proxy);
    }
}
