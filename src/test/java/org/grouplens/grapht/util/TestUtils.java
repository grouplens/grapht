package org.grouplens.grapht.util;

import java.io.*;
import java.lang.reflect.Field;

/**
 * Utilities for helping with test cases.
 */
public class TestUtils {
    /**
     * Serialize and deserialize an object.
     * @param obj The object to serialize
     * @return An object that is the result of serializing and deserializing {@code obj}.
     */
    @SuppressWarnings("unchecked")
    public static <T> T serializeRoundTrip(T obj) throws IOException, ClassNotFoundException {
        // serialize the proxy
        ByteArrayOutputStream str = new ByteArrayOutputStream();
        ObjectOutput oout = new ObjectOutputStream(str);
        oout.writeObject(obj);
        oout.close();
        byte[] bytes = str.toByteArray();

        // read the proxy back in
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInput oin = new ObjectInputStream(in);
        try {
            return (T) obj.getClass().cast(oin.readObject());
        } finally {
            oin.close();
        }
    }
}
