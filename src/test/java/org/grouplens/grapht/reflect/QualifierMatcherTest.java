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
