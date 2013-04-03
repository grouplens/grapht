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
    public static <T extends Serializable> T serializeRoundTrip(T obj) throws IOException, ClassNotFoundException {
        byte[] bytes = SerializationUtils.serialize(obj);
        return (T) obj.getClass().cast(SerializationUtils.deserialize(bytes));
    }
}
