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
