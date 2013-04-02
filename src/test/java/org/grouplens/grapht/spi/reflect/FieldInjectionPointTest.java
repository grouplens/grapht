package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.util.TestUtils;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
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
        FieldInjectionPoint fip2 = TestUtils.serializeRoundTrip(fip);
        assertThat(fip2, equalTo(fip));
        assertThat(fip2, not(sameInstance(fip)));
    }
}
