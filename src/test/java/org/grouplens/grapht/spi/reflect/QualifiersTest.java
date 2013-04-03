package org.grouplens.grapht.spi.reflect;

import org.apache.commons.lang3.SerializationUtils;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Qualifier;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class QualifiersTest {
    @Qualifier
    public static @interface Qual {}
    @Qualifier
    public static @interface VQual {
        String value();
    }

    private Qual makeQual() {
        return AnnotationBuilder.of(Qual.class).build();
    }
    private VQual makeVQual(String val) {
        return AnnotationBuilder.of(VQual.class).set("value", val).build();
    }

    @Test
    public void testIsQualifier() throws Exception {
        assertThat(Qualifiers.isQualifier(Nullable.class),
                   equalTo(false));
        assertThat(Qualifiers.isQualifier(Qual.class),
                   equalTo(true));
    }

    @Test
    public void testMatchAny() throws Exception {
        assertThat(Qualifiers.matchAny().matches(null),
                   equalTo(true));
        assertThat(Qualifiers.matchAny().matches(makeQual()),
                   equalTo(true));
        assertThat(Qualifiers.matchAny().matches(makeVQual("foo")),
                   equalTo(true));
        assertThat(Qualifiers.matchAny(), equalTo(Qualifiers.matchAny()));
        assertThat(SerializationUtils.clone(Qualifiers.matchAny()),
                   equalTo(Qualifiers.matchAny()));
    }

    @Test
    public void testMatchNone() throws Exception {
        assertThat(Qualifiers.matchNone().matches(null),
                   equalTo(true));
        assertThat(Qualifiers.matchNone().matches(makeQual()),
                   equalTo(false));
        assertThat(Qualifiers.matchNone().matches(makeVQual("foo")),
                   equalTo(false));
        assertThat(Qualifiers.matchNone(), equalTo(Qualifiers.matchNone()));
        assertThat(Qualifiers.matchNone(), not(equalTo(Qualifiers.matchAny())));
        assertThat(SerializationUtils.clone(Qualifiers.matchNone()),
                   equalTo(Qualifiers.matchNone()));
    }

    @Test
    public void testMatchClass() throws Exception {
        assertThat(Qualifiers.match(Qual.class).matches(null),
                   equalTo(false));
        assertThat(Qualifiers.match(Qual.class).matches(makeQual()),
                   equalTo(true));
        assertThat(Qualifiers.match(Qual.class).matches(makeVQual("foo")),
                   equalTo(false));
        assertThat(Qualifiers.match(VQual.class).matches(makeVQual("foo")),
                   equalTo(true));
        assertThat(Qualifiers.match(Qual.class), equalTo(Qualifiers.match(Qual.class)));
        assertThat(Qualifiers.match(Qual.class), not(equalTo(Qualifiers.match(VQual.class))));
        assertThat(Qualifiers.match(Qual.class), not(equalTo(Qualifiers.matchAny())));
        assertThat(SerializationUtils.clone(Qualifiers.match(Qual.class)),
                   equalTo(Qualifiers.match(Qual.class)));
        assertThat(SerializationUtils.clone(Qualifiers.match(VQual.class)),
                   equalTo(Qualifiers.match(VQual.class)));
    }

    @Test
    public void testMatchValue() throws Exception {
        assertThat(Qualifiers.match(makeQual()).matches(null),
                   equalTo(false));
        assertThat(Qualifiers.match(makeQual()).matches(makeQual()),
                   equalTo(true));
        assertThat(Qualifiers.match(makeQual()).matches(makeVQual("foo")),
                   equalTo(false));
        assertThat(Qualifiers.match(makeVQual("foo")).matches(makeVQual("foo")),
                   equalTo(true));
        assertThat(Qualifiers.match(makeVQual("bar")).matches(makeVQual("foo")),
                   equalTo(false));
        assertThat(Qualifiers.match(makeQual()),
                   equalTo(Qualifiers.match(makeQual())));
        assertThat(Qualifiers.match(makeVQual("foo")),
                   equalTo(Qualifiers.match(makeVQual("foo"))));
        assertThat(Qualifiers.match(makeVQual("foo")),
                   not(equalTo(Qualifiers.match(makeVQual("bar")))));
        assertThat(SerializationUtils.clone(Qualifiers.match(makeQual())),
                   equalTo(Qualifiers.match(makeQual())));
        assertThat(SerializationUtils.clone(Qualifiers.match(makeVQual("foo"))),
                   equalTo(Qualifiers.match(makeVQual("foo"))));
    }
}
