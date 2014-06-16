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
package org.grouplens.grapht.util;

import org.junit.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class AbstractChainTest {
    static class TestChain extends AbstractChain<String> {
        TestChain(AbstractChain<String> prev, String tv) {
            super(prev, tv);
        }
        public TestChain extend(String tok) {
            return new TestChain(this, tok);
        }
    }

    static TestChain singleton(String tok) {
        return new TestChain(null, tok);
    }
    static TestChain chain(String t1, String... toks) {
        TestChain chain = singleton(t1);
        for (String tok: toks) {
            chain = chain.extend(tok);
        }
        return chain;
    }

    @Test
    public void testSingletonBasicProps() throws Exception {
        TestChain chain = singleton("foo");
        assertThat(chain.size(), equalTo(1));
        assertThat(chain.isEmpty(), equalTo(false));
        assertThat(chain.contains("foo"), equalTo(true));
        assertThat(chain, contains("foo"));
    }

    @Test
    public void testSingletonsEqual() {
        TestChain foo1 = singleton("foo");
        TestChain foo2 = singleton("foo");
        TestChain bar = singleton("bar");

        assertThat(foo1.equals(foo1), equalTo(true));
        assertThat(foo1.equals(foo2), equalTo(true));
        assertThat(foo1.equals(bar), equalTo(false));
    }

    @Test
    public void testSingletonGet() {
        TestChain foo = singleton("foo");

        assertThat(foo.get(0), equalTo("foo"));
    }

    @Test
    public void testExtend() {
        TestChain foo = singleton("foo");
        TestChain foobar = foo.extend("bar");

        assertThat(foobar.size(), equalTo(2));

        Iterator<String> iter = foobar.iterator();
        assertThat(iter.next(), equalTo("foo"));
        assertThat(iter.next(), equalTo("bar"));
        assertThat(iter.hasNext(), equalTo(false));

        assertThat(foobar.get(1), equalTo("bar"));
        assertThat(foobar.get(0), equalTo("foo"));
    }

    @Test
    public void testReverseIteration() {
        TestChain chain = chain("foobie", "bletch", "forever");
        assertThat(chain, hasSize(3));
        assertThat(chain.reverse(), contains("forever", "bletch", "foobie"));

        Iterator<String> iter = chain.reverseIterator();
        iter.next();
        iter.next();
        iter.next();
        try {
            iter.next();
            fail("must throw NoSuchElement");
        } catch (Throwable th) {
            assertThat(th, instanceOf(NoSuchElementException.class));
        }
    }

    @Test
    public void testChainsEqual() {
        TestChain base = chain("foo", "bar", "blatz");
        TestChain copy = chain("foo", "bar", "blatz");
        TestChain chend = chain("foo", "bar", "blam");
        TestChain chstart = chain("f00", "bar", "blatz");

        assertThat(base.equals(base), equalTo(true));
        assertThat(base.equals(copy), equalTo(true));
        assertThat(base.equals(chend), equalTo(false));
        assertThat(base.equals(chstart), equalTo(false));
    }
}
