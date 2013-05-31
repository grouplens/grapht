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

import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QualifierMatcherTest {
    private InjectSPI spi;
    
    @Before
    public void setup() {
        spi = new ReflectionInjectSPI();
    }
    
    @Test
    public void testAnnotationInstanceMatch() {
        QualifierMatcher matcher = spi.match(new AnnotationBuilder<Named>(Named.class).set("value", "test").build());
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertFalse(matcher.matches(null));
    }
    
    @Test
    public void testAnnotationClassMatch() {
        QualifierMatcher matcher = spi.match(Named.class);
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertFalse(matcher.matches(null));
    }
    
    @Test
    public void testAnyMatch() {
        QualifierMatcher matcher = spi.matchAny();
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertTrue(matcher.matches(null));
    }
    
    @Test
    public void testNoContextMatch() {
        QualifierMatcher matcher = spi.matchNone();
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertTrue(matcher.matches(null));
    }
    
    @Test
    public void testComparator() {
        QualifierMatcher m1 = spi.match(new AnnotationBuilder<Named>(Named.class).set("value", "test").build());
        QualifierMatcher m2 = spi.match(Named.class);
        QualifierMatcher m3 = spi.matchAny();
        QualifierMatcher m4 = spi.matchNone();
        
        List<QualifierMatcher> ordered = Arrays.asList(m3, m2, m4, m1); // purposely unordered
        List<QualifierMatcher> expected = Arrays.asList(m4, m1, m2, m3); // m4, and m1 are equal, but its a consistent ordering
        
        Collections.sort(ordered);
        Assert.assertEquals(expected, ordered);
    }
}
