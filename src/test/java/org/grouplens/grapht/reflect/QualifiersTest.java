/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht.reflect;

import org.apache.commons.lang3.SerializationUtils;
import org.grouplens.grapht.annotation.AliasFor;
import org.grouplens.grapht.annotation.AllowUnqualifiedMatch;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class QualifiersTest {
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Qual {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @AliasFor(Qual.class)
    public static @interface AQual {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @AliasFor(AQual.class)
    public static @interface AAQual {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @AliasFor(Qual.class)
    public static @interface BadAlias {
        /* aliases cannot have values */
        String value();
    }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @AliasFor(Circle2.class)
    public static @interface Circle1 {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @AliasFor(Circle1.class)
    public static @interface Circle2 {}

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface VQual {
        String value();
    }
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @AllowUnqualifiedMatch
    public static @interface DftQual {}

    private Qual makeQual() {
        return AnnotationBuilder.of(Qual.class).build();
    }
    private VQual makeVQual(String val) {
        return AnnotationBuilder.of(VQual.class).set("value", val).build();
    }
    private DftQual makeDftQual() {
        return AnnotationBuilder.of(DftQual.class).build();
    }

    @Test
    public void testIsQualifier() throws Exception {
        assertThat(Qualifiers.isQualifier(Nullable.class),
                   equalTo(false));
        assertThat(Qualifiers.isQualifier(Qual.class),
                   equalTo(true));
    }

    @Test
    public void testResolveAliasUnaliased() {
        assertThat(Qualifiers.resolveAliases(Qual.class),
                   equalTo((Class) Qual.class));
    }

    @Test
    public void testResolveAlias() {
        assertThat(Qualifiers.resolveAliases(AQual.class),
                   equalTo((Class) Qual.class));
    }

    @Test
    public void testResolveDoubleAlias() {
        assertThat(Qualifiers.resolveAliases(AAQual.class),
                   equalTo((Class) Qual.class));
    }

    @Test
    public void testRejectBadAlias() {
        try {
            Qualifiers.resolveAliases(BadAlias.class);
            fail("resolving a bad alias should throw an exception");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }

    @Test
    public void testRejectCircularAlias() {
        try {
            Qualifiers.resolveAliases(Circle2.class);
            fail("resolving a circular alias should throw an exception");
        } catch (IllegalArgumentException ex) {
            /* expected */
        }
    }

    @Test
    public void testMatchAny() throws Exception {
        assertThat(Qualifiers.matchAny().matches(null),
                   equalTo(true));
        assertThat(Qualifiers.matchAny().matches(makeQual()),
                   equalTo(true));
        assertThat(Qualifiers.matchAny().matches(makeVQual("foo")),
                   equalTo(true));
        assertThat(Qualifiers.matchAny().matches(makeDftQual()),
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
        assertThat(Qualifiers.matchNone().matches(makeDftQual()),
                   equalTo(false));
        assertThat(Qualifiers.matchNone(), equalTo(Qualifiers.matchNone()));
        assertThat(Qualifiers.matchNone(), not(equalTo(Qualifiers.matchAny())));
        assertThat(SerializationUtils.clone(Qualifiers.matchNone()),
                   equalTo(Qualifiers.matchNone()));
    }

    @Test
    public void testMatchDefault() throws Exception {
        assertThat(Qualifiers.matchDefault().matches(null),
                   equalTo(true));
        assertThat(Qualifiers.matchDefault().matches(makeQual()),
                   equalTo(false));
        assertThat(Qualifiers.matchDefault().matches(makeVQual("foo")),
                   equalTo(false));
        assertThat(Qualifiers.matchDefault().matches(makeDftQual()),
                   equalTo(true));
        assertThat(Qualifiers.matchDefault(), equalTo(Qualifiers.matchDefault()));
        assertThat(Qualifiers.matchDefault(), not(equalTo(Qualifiers.matchAny())));
        assertThat(SerializationUtils.clone(Qualifiers.matchDefault()),
                   equalTo(Qualifiers.matchDefault()));
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
