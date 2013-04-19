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
package org.grouplens.grapht.spi.reflect;

import org.apache.commons.lang3.SerializationUtils;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

public class QualifiersTest {
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Qual {}
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
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
