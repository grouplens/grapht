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
package org.grouplens.grapht.spi.context;

import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.MockSatisfaction;
import org.grouplens.grapht.spi.reflect.AttributesImpl;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ContextPatternTest {
    @Test
    public void testSingletonChainEmptyContextSuccess() throws Exception {
        // Test that a singleton pattern matches an empty context
        assertThat(ContextPattern.empty().append(ContextElements.matchAny()).matches(makeContext()),
                   notNullValue());
    }

    @Test
    public void testAnyChainEmptyContextSuccess() throws Exception {
        // Test that the 'any' pattern matches an empty context
        assertThat(ContextPattern.any().matches(makeContext()),
                   notNullValue());
    }

    @Test
    public void testEmptyChainNonEmptyContextFailure() throws Exception {
        assertThat(ContextPattern.empty().matches(makeContext(A.class)),
                   nullValue());
    }

    @Test
    public void testSingleton() {
        // test a single type pattern
        // should match itself
        ContextPattern initial = ContextPattern.empty().append(ContextElements.matchAny());
        assertThat(initial.append(A.class).matches(makeContext(A.class)),
                   notNullValue());
        // should not match other type
        assertThat(initial.append(A.class).matches(makeContext(B.class)),
                   nullValue());
        // should not match empty
        assertThat(initial.append(A.class).matches(makeContext()),
                   nullValue());
        // should not match too long
        assertThat(initial.append(A.class).matches(makeContext(A.class, B.class)),
                   nullValue());
        // either way
        assertThat(initial.append(A.class).matches(makeContext(B.class, A.class)),
                   nullValue());
        // either way
        assertThat(initial.append(A.class).matches(makeContext(B.class, A.class)),
                   nullValue());
        // or duplicated
        assertThat(initial.append(A.class).matches(makeContext(A.class, A.class)),
                   nullValue());
    }
    
    @Test
    public void testAnyChainNonEmptyContextSuccess() throws Exception {
        assertThat(ContextPattern.any().matches(makeContext(A.class)),
                   notNullValue());
        assertThat(ContextPattern.any().matches(makeContext(A.class, B.class)),
                   notNullValue());
        assertThat(ContextPattern.any().matches(makeContext(A.class, Ap.class)),
                   notNullValue());
        assertThat(ContextPattern.any().matches(makeContext(A.class, B.class, C.class)),
                   notNullValue());
    }
    
    @Test
    public void testSubsequenceEqualChainContextSuccess() throws Exception {
        // Test that a subsequence pattern matches
        assertThat(ContextPattern.subsequence(A.class)
                                 .matches(makeContext(A.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class)
                                 .matches(makeContext(A.class, B.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class, C.class)
                                 .matches(makeContext(A.class, B.class, C.class)),
                   notNullValue());
    }
    
    @Test
    public void testSubstringChainSuccess() throws Exception {
        assertThat(ContextPattern.subsequence(A.class)
                                 .matches(makeContext(A.class, B.class, C.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(B.class)
                                 .matches(makeContext(A.class, B.class, C.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(C.class)
                                 .matches(makeContext(A.class, B.class, C.class)), notNullValue());

        assertThat(ContextPattern.subsequence(A.class, B.class)
                                 .matches(makeContext(A.class, B.class, C.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(B.class, C.class)
                                 .matches(makeContext(A.class, B.class, C.class)), notNullValue());
        
        assertThat(ContextPattern.subsequence(B.class, C.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class)),
                   notNullValue());
    }
    
    @Test
    public void testSubsequenceChainSuccess() throws Exception {
        assertThat(ContextPattern.subsequence(A.class, C.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, Ap.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, Bp.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, C.class, Bp.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class, Ap.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(B.class, Cp.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(A.class, Cp.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
        assertThat(ContextPattern.subsequence(C.class, Ap.class, Bp.class)
                                 .matches(makeContext(A.class, B.class, C.class, Ap.class, Bp.class, Cp.class)),
                   notNullValue());
    }
    
    @Test
    public void testMatcherInheritenceSuccess() throws Exception {
        assertThat(ContextPattern.subsequence(A.class).matches(makeContext(Ap.class)), notNullValue());

        assertThat(ContextPattern.subsequence(A.class, C.class).matches(makeContext(Ap.class, Cp.class)), notNullValue());
        assertThat(ContextPattern.subsequence(A.class, C.class).matches(makeContext(A.class, Cp.class)), notNullValue());
        assertThat(ContextPattern.subsequence(A.class, C.class).matches(makeContext(Ap.class, C.class)), notNullValue());
    }
    
    @Test
    public void testNonSubsequenceFail() throws Exception {
        assertThat(ContextPattern.subsequence(A.class).matches(makeContext(B.class)), nullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class).matches(makeContext(B.class, A.class)), nullValue());
        assertThat(ContextPattern.subsequence(B.class, A.class, C.class).matches(makeContext(C.class, B.class, A.class)), nullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class, C.class).matches(makeContext(C.class, B.class, A.class)), nullValue());
    }
    
    @Test
    public void testSuperstringFail() throws Exception {
        assertThat(ContextPattern.subsequence(A.class, B.class).matches(makeContext(A.class)), nullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class, C.class).matches(makeContext(A.class, C.class)), nullValue());
        assertThat(ContextPattern.subsequence(A.class, B.class, C.class).matches(makeContext(A.class, B.class)), nullValue());
    }

    @Test
    public void testTailAnchoredMatch() {
        ContextMatcher matcher = ContextPattern.any().append(A.class);
        assertThat(matcher.matches(makeContext()),
                   nullValue());
        assertThat(matcher.matches(makeContext(A.class)),
                   notNullValue());
        assertThat(matcher.matches(makeContext(B.class, A.class)),
                   notNullValue());
        assertThat(matcher.matches(makeContext(A.class, B.class)),
                   nullValue());
    }

    @Test
    public void testAnchoredAndUnanchored() {
        ContextMatcher matcher = ContextPattern.any()
                                               .append(A.class)
                                               .append(B.class)
                                               .appendDotStar();
        assertThat(matcher.matches(makeContext()),
                   nullValue());
        assertThat(matcher.matches(makeContext(A.class, B.class)),
                   notNullValue());
        assertThat(matcher.matches(makeContext(A.class, B.class, C.class)),
                   notNullValue());
        assertThat(matcher.matches(makeContext(A.class, C.class, B.class)),
                   nullValue());
    }

    @Test
    public void testOrderByCloseness() {
        ContextPattern patA = ContextPattern.subsequence(A.class);
        ContextPattern patB = ContextPattern.subsequence(B.class);
        InjectionContext ctx1 = makeContext(A.class, B.class);
        // B matches more closely than A
        assertThat(patB.matches(ctx1), lessThan(patA.matches(ctx1)));
        // A matches the same as itself
        assertThat(patA.matches(ctx1),
                   allOf(lessThanOrEqualTo(patA.matches(ctx1)),
                         greaterThanOrEqualTo(patA.matches(ctx1))));
    }

    @Test
    public void testOrderByLength() {
        ContextPattern patShort = ContextPattern.subsequence(B.class);
        ContextPattern patLong = ContextPattern.subsequence(A.class, B.class);
        InjectionContext ctx1 = makeContext(A.class, B.class);
        // Long matches more closely than short
        assertThat(patLong.matches(ctx1), lessThan(patShort.matches(ctx1)));
        // Long matches like itself.
        assertThat(patLong.matches(ctx1),
                   allOf(lessThanOrEqualTo(patLong.matches(ctx1)),
                         greaterThanOrEqualTo(patLong.matches(ctx1))));
    }

    @Test
    public void testOrderByLengthAfterCloseness() {
        ContextPattern patClose = ContextPattern.subsequence(C.class);
        ContextPattern patFar = ContextPattern.subsequence(A.class, B.class);
        InjectionContext ctx1 = makeContext(A.class, B.class, C.class);
        // Close matches more closely than long
        assertThat(patClose.matches(ctx1), lessThan(patFar.matches(ctx1)));
    }

    @Test
    public void testOrderByType() {
        ContextPattern patStrict = ContextPattern.subsequence(Ap.class);
        ContextPattern patLoose = ContextPattern.subsequence(A.class);
        InjectionContext ctx1 = makeContext(Ap.class);
        // Tight matches more tightly
        assertThat(patStrict.matches(ctx1),
                   lessThan(patLoose.matches(ctx1)));
    }

    @Test
    public void testOrderByTypeLast() {
        ContextPattern patStrict = ContextPattern.subsequence(Bp.class);
        ContextPattern patLong = ContextPattern.subsequence(A.class, B.class);
        InjectionContext ctx1 = makeContext(A.class, Bp.class);
        // Length trumps tightness
        assertThat(patLong.matches(ctx1),
                   lessThan(patStrict.matches(ctx1)));
    }

    private InjectionContext makeContext(Class<?>... types) {
        InjectionContext context = DependencySolver.initialContext();
        for (Class<?> type: types) {
            MockSatisfaction sat = new MockSatisfaction(type, new ArrayList<Desire>());
            context = context.extend(sat, new AttributesImpl());
        }
        return context;
    }
    
    private static class A {}
    private static class B {}
    private static class C {}
    
    private static class Ap extends A {}
    private static class Bp extends B {}
    private static class Cp extends C {}
}
