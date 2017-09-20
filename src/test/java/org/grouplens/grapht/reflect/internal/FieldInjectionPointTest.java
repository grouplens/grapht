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
        assertThat(nonnull.isOptional(), equalTo(false));
    }

    @Test
    public void testNullable() throws NoSuchFieldException {
        FieldInjectionPoint field = new FieldInjectionPoint(getClass().getDeclaredField("nullable"));
        assertThat(field.isOptional(), equalTo(true));
    }

    @Test
    public void testSerialize() throws NoSuchFieldException, IOException, ClassNotFoundException {
        FieldInjectionPoint fip = new FieldInjectionPoint(getClass().getDeclaredField("foo"));
        FieldInjectionPoint fip2 = SerializationUtils.clone(fip);
        assertThat(fip2, equalTo(fip));
        assertThat(fip2, not(sameInstance(fip)));
    }
}
