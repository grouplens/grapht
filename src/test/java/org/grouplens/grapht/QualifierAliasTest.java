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
package org.grouplens.grapht;

import org.grouplens.grapht.annotation.AliasFor;
import org.grouplens.grapht.annotation.DefaultNull;
import org.grouplens.grapht.solver.MultipleBindingsException;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for qualifier aliases.
 *
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class QualifierAliasTest {

    /**
     * Test that bindings and dependencies on unaliased qualifier work.
     */
    @Test
    public void testRealQualifier() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Qual.class, String.class).to("hello");
        Injector inj = bld.build();
        RequireQual rq = inj.getInstance(RequireQual.class);
        assertThat(rq.dependency, equalTo("hello"));
    }

    /**
     * Test that binding to alias w/ dep on unaliased qualifier works.
     */
    @Test
    public void testBindAlias() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Alias.class, String.class).to("hello");
        Injector inj = bld.build();
        RequireQual rq = inj.getInstance(RequireQual.class);
        assertThat(rq.dependency, equalTo("hello"));
    }

    /**
     * Test that binding to qualifier w/ dep on alias works.
     */
    @Test
    public void testRequireAlias() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Qual.class, String.class).to("hello");
        Injector inj = bld.build();
        RequireAlias rq = inj.getInstance(RequireAlias.class);
        assertThat(rq.dependency, equalTo("hello"));
    }

    /**
     * Test that binding and requiring alias works.
     */
    @Test
    public void testBindAndRequireAlias() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Alias.class, String.class).to("hello");
        Injector inj = bld.build();
        RequireAlias rq = inj.getInstance(RequireAlias.class);
        assertThat(rq.dependency, equalTo("hello"));
    }

    /**
     * Test that binding both alias and qualifier fails.
     */
    @Test
    public void testBindQualAndAlias() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        bld.bind(Alias.class, String.class).to("goodbye");
        bld.bind(Qual.class, String.class).to("hello");
        Injector inj = bld.build();
        try {
            RequireAlias rq = inj.getInstance(RequireAlias.class);
            fail("should fail w/ multiple bindings");
        } catch (MultipleBindingsException ex) {
            /* expected */
        }
    }

    /**
     * Basic qualifier
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @Documented
    public static @interface Qual {}

    /**
     * Qualifier Alias
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @Documented
    @AliasFor(Qual.class)
    public static @interface Alias {}

    public static class RequireQual {
        final String dependency;

        @Inject
        public RequireQual(@Qual String dep) {
            dependency = dep;
        }
    }

    public static class RequireAlias {
        final String dependency;

        @Inject
        public RequireAlias(@Alias String dep) {
            dependency = dep;
        }
    }
}
