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

import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.grapht.reflect.internal.types.RoleA;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class QualifierMatcherTest {
    @Test
    public void testAnnotationInstanceMatch() {
        QualifierMatcher matcher = Qualifiers.match(new AnnotationBuilder<Named>(Named.class).set("value", "test").build());
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertFalse(matcher.matches(null));
    }
    
    @Test
    public void testAnnotationClassMatch() {
        QualifierMatcher matcher = Qualifiers.match(Named.class);
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertFalse(matcher.matches(null));
    }
    
    @Test
    public void testAnyMatch() {
        QualifierMatcher matcher = Qualifiers.matchAny();
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertTrue(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertTrue(matcher.matches(null));
    }
    
    @Test
    public void testNoContextMatch() {
        QualifierMatcher matcher = Qualifiers.matchNone();
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<Named>(Named.class).set("value", "not-test").build()));
        Assert.assertFalse(matcher.matches(new AnnotationBuilder<RoleA>(RoleA.class).build()));
        Assert.assertTrue(matcher.matches(null));
    }
    
    @Test
    public void testComparator() {
        QualifierMatcher m1 = Qualifiers.match(new AnnotationBuilder<Named>(Named.class).set("value", "test").build());
        QualifierMatcher m2 = Qualifiers.match(Named.class);
        QualifierMatcher m3 = Qualifiers.matchAny();
        QualifierMatcher m4 = Qualifiers.matchNone();
        
        List<QualifierMatcher> ordered = Arrays.asList(m3, m2, m4, m1); // purposely unordered
        List<QualifierMatcher> expected = Arrays.asList(m4, m1, m2, m3); // m4, and m1 are equal, but its a consistent ordering
        
        Collections.sort(ordered);
        Assert.assertEquals(expected, ordered);
    }
}
