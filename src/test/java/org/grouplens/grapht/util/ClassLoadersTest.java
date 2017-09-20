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

import java.net.URL;
import java.net.URLClassLoader;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

/**
 * Tests for the class loader utilities.
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ClassLoadersTest {
    @Test
    public void testPushContextClassLoader() throws Exception {
        Thread th = Thread.currentThread();
        ClassLoader orig = th.getContextClassLoader();
        ClassLoader loader = new URLClassLoader(new URL[0], ClassLoaders.inferDefault(ClassLoadersTest.class));
        ClassLoaderContext ctx = ClassLoaders.pushContext(loader);
        try {
            assertThat(th.getContextClassLoader(), sameInstance(loader));
        } finally {
            ctx.pop();
        }
        assertThat(th.getContextClassLoader(), sameInstance(orig));
        try {
            ctx.pop();
            fail("re-popping context should be illegal state");
        } catch (IllegalStateException e) {
            /* expected */
        }
    }

    @Test
    public void testInferLoaderFromRefClass() throws Exception {
        Thread th = Thread.currentThread();
        ClassLoader orig = th.getContextClassLoader();
        assumeThat("have a context class loader",
                   orig, notNullValue());
        ClassLoaderContext ctx = ClassLoaders.pushContext(null);
        try {
            assertThat(ClassLoaders.inferDefault(ClassLoadersTest.class),
                       equalTo(getClass().getClassLoader()));
        } finally {
            ctx.pop();
        }
    }

    @Test
    public void testInferLoaderFromThread() throws Exception {
        Thread th = Thread.currentThread();
        ClassLoader orig = th.getContextClassLoader();
        assumeThat("have a context class loader",
                   orig, notNullValue());
        assertThat(ClassLoaders.inferDefault(ClassLoadersTest.class),
                   equalTo(orig));
    }
}
