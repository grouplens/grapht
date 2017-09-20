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
package org.grouplens.grapht.context;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.MockInjectionPoint;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.ClassSatisfaction;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.grapht.reflect.internal.types.RoleA;
import org.grouplens.grapht.reflect.internal.types.RoleB;
import org.grouplens.grapht.reflect.internal.types.RoleD;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class TypeElementMatcherTest {
    @Test
    public void testEquals() {
        TypeElementMatcher m1 = new TypeElementMatcher(A.class);
        TypeElementMatcher m2 = new TypeElementMatcher(B.class);
        TypeElementMatcher m3 = new TypeElementMatcher(A.class, Qualifiers.match(RoleA.class));
        TypeElementMatcher m4 = new TypeElementMatcher(A.class, Qualifiers.match(RoleB.class));
        
        Assert.assertEquals(m1, new TypeElementMatcher(A.class));
        Assert.assertEquals(m2, new TypeElementMatcher(B.class));
        Assert.assertEquals(m3, new TypeElementMatcher(A.class, Qualifiers.match(RoleA.class)));
        Assert.assertEquals(m4, new TypeElementMatcher(A.class, Qualifiers.match(RoleB.class)));
        
        Assert.assertFalse(m1.equals(m2));
        Assert.assertFalse(m2.equals(m3));
        Assert.assertFalse(m3.equals(m4));
        Assert.assertFalse(m4.equals(m1));
    }
    
    @Test
    public void testExactClassNoRoleMatch() {
        doTestMatch(A.class, null, A.class, null, true);
        doTestMatch(C.class, null, C.class, null, true);
    }
    
    @Test
    public void testExactClassExactRoleMatch() { 
        doTestMatch(A.class, RoleA.class, A.class, RoleA.class, true);
    }
    
    @Test
    public void testSubclassNoRoleMatch() {
        doTestMatch(A.class, null, B.class, null, true);
    }
    
    @Test
    public void testNoClassInheritenceSubRoleNoMatch() {
        doTestMatch(C.class, RoleA.class, A.class, RoleB.class, false);
        doTestMatch(B.class, RoleA.class, A.class, RoleB.class, false);
    }
    
    @Test
    public void testSubclassNoRoleInheritenceNoMatch() {
        doTestMatch(A.class, RoleA.class, B.class, RoleD.class, false);
    }

    @Test
    public void testNull() {
        doTestMatch(null, null, null, null, true);
        doTestMatch(null, null, A.class, null, false);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void doTestMatch(Class<?> matcherType, Class<? extends Annotation> matcherRole,
                             Class<?> satisfactionType, Class<? extends Annotation> satisfactionRole, 
                             boolean expected) {
        QualifierMatcher mr = (matcherRole == null ? Qualifiers.matchDefault() : Qualifiers.match(matcherRole));
        final Annotation sr = (satisfactionRole == null ? null : new AnnotationBuilder(satisfactionRole).build());
        Satisfaction sat = null;
        if (satisfactionType != null) {
            sat = new ClassSatisfaction(satisfactionType);
        }
        InjectionPoint ip = new MockInjectionPoint(satisfactionType, sr, false);
        Pair<Satisfaction, InjectionPoint> node = Pair.of(sat, ip);
        
        TypeElementMatcher cm = new TypeElementMatcher(matcherType, mr);
        if (expected) {
            Assert.assertThat(cm.apply(node), notNullValue());
        } else {
            Assert.assertThat(cm.apply(node), nullValue());
        }
    }
    
    public static class A { }
    
    public static class B extends A { }
    
    public static class C { }
}
