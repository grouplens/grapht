package org.grouplens.grapht.util;

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
        FieldProxy proxy = FieldProxy.forField(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeField() throws NoSuchFieldException, ClassNotFoundException, IOException {
        Field f = TestClass.class.getDeclaredField("foo");
        FieldProxy proxy = roundTrip(f);
        assertThat(proxy.resolve(), equalTo(f));
    }

    @Test
    public void testSerializeArrayField() throws NoSuchFieldException, ClassNotFoundException, IOException {
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
        FieldProxy proxy = FieldProxy.forField(fld);
        return TestUtils.serializeRoundTrip(proxy);
    }
}
