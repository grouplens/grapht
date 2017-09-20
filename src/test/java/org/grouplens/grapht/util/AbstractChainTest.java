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
