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
package org.grouplens.grapht.reflect.internal;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class FieldInjectionPointTest {
    // a field to test serialization
    String foo;
    @Nullable
    String nullable;

    @Test
    public void testNotNullable() throws NoSuchFieldException {
        FieldInjectionPoint nonnull = new FieldInjectionPoint(getClass().getDeclaredField("foo"));
        assertThat(nonnull.isNullable(), equalTo(false));
    }

    @Test
    public void testNullable() throws NoSuchFieldException {
        FieldInjectionPoint field = new FieldInjectionPoint(getClass().getDeclaredField("nullable"));
        assertThat(field.isNullable(), equalTo(true));
    }

    @Test
    public void testSerialize() throws NoSuchFieldException, IOException, ClassNotFoundException {
        FieldInjectionPoint fip = new FieldInjectionPoint(getClass().getDeclaredField("foo"));
        FieldInjectionPoint fip2 = SerializationUtils.clone(fip);
        assertThat(fip2, equalTo(fip));
        assertThat(fip2, not(sameInstance(fip)));
    }
}
