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
package org.grouplens.grapht.util;

import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.InjectorBuilder;
import org.junit.Test;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

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
public class ComponentLifecycleTest {
    @Test
    public void testClosesAutoCloseable() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Flag closed = new Flag();
        bld.bind(Flag.class).to(closed);
        Injector injector = bld.build();
        try {
            CloseableComponent comp = injector.getInstance(CloseableComponent.class);
            assertThat(comp, notNullValue());
        } finally {
            injector.close();
        }
        // Check that close was called.
        assertTrue("close was called", closed.isSet());
    }

    /**
     * If we inject an instance of an AutoCloseable, it should not be closed.
     */
    @Test
    public void testDoesNotCloseAutoCloseableInstance() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Flag closed = new Flag();
        bld.bind(Flag.class).to(closed);
        bld.bind(CloseableComponent.class).to(new CloseableComponent(closed));
        Injector injector = bld.build();
        try {
            CloseableComponent comp = injector.getInstance(CloseableComponent.class);
            assertThat(comp, notNullValue());
        } finally {
            injector.close();
        }
        // Check that close was not called.
        assertFalse("close was called", closed.isSet());
    }

    /**
     * Test that the lifecycle management calls pre-destroy methods.
     */
    @Test
    public void testCallsPreDestroy() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Flag closed = new Flag();
        bld.bind(Flag.class).to(closed);
        Injector injector = bld.build();
        try {
            LifecycleShutdownComponent comp = injector.getInstance(LifecycleShutdownComponent.class);
            assertThat(comp, notNullValue());
        } finally {
            injector.close();
        }
        // Check that close was called.
        assertTrue("close was called", closed.isSet());
    }

    /**
     * Test that the lifecycle management does not call pre-destroy methods on an instance.
     */
    @Test
    public void testDoesNotCallPreDestroyOnInstance() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Flag closed = new Flag();
        bld.bind(LifecycleShutdownComponent.class).to(new LifecycleShutdownComponent(closed));
        Injector injector = bld.build();
        try {
            LifecycleShutdownComponent comp = injector.getInstance(LifecycleShutdownComponent.class);
            assertThat(comp, notNullValue());
        } finally {
            injector.close();
        }
        // Check that close was called.
        assertFalse("close was called", closed.isSet());
    }

    /**
     * Test that the lifecycle management calls post-construct methods
     */
    @Test
    public void testCallsPostConstruct() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Flag setup = new Flag();
        bld.bind(Flag.class).to(setup);
        Injector injector = bld.build();
        try {
            PostConstructComponent comp = injector.getInstance(PostConstructComponent.class);
            assertThat(comp, notNullValue());
            assertTrue("setup was called", setup.isSet());
        } finally {
            injector.close();
        }
        // Check that close was called.
        assertTrue("close was called", setup.isSet());
    }

    /**
     * Flag component for detecting closure.
     */
    public static class Flag {
        private boolean value = false;

        public void set() {
            value = true;
        }

        public boolean isSet() {
            return value;
        }
    }

    /**
     * Component implementing AutoCloseable that should be closed.
     */
    public static class CloseableComponent implements AutoCloseable {
        private final Flag flag;

        @Inject
        public CloseableComponent(Flag f) {
            flag = f;
        }

        @Override
        public void close() throws Exception {
            flag.set();
        }
    }

    /**
     * Component with PreDestroy method that must be called.
     */
    public static class LifecycleShutdownComponent {
        private final Flag flag;

        @Inject
        public LifecycleShutdownComponent(Flag f) {
            flag = f;
        }

        @PreDestroy
        public void shutdown() {
            flag.set();
        }
    }

    /**
     * component with post-construct methods
     */
    public static class PostConstructComponent {
        private Flag setup;

        // we use setter injection to make sure PostConstruct is called after setters
        @Inject
        public void setFlag(Flag f) {
            setup = f;
        }

        @PostConstruct
        public void setup() {
            assertThat(setup, notNullValue());
            setup.set();
        }
    }
}
