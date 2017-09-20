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
package org.grouplens.grapht;

import org.grouplens.grapht.annotation.AliasFor;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.annotation.DefaultNull;
import org.grouplens.grapht.annotation.DefaultString;
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
     * Test that binding to alias value w/ dep on unaliased qualifier works.
     */
    @Test
    public void testBindAliasValue() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Alias qual = AnnotationBuilder.of(Alias.class).build();
        bld.bind(String.class).withQualifier(qual).to("hello");
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
     * Test that binding both alias and qualifier fails.  Aliases are treated as equivalent to their qualifiers,
     * so we have a multiple-binding situation.
     *
     * We will also take this opportunity to test the {@link MultipleBindingsException}.
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
            assertThat(ex.getBindRules(), hasSize(2));
            assertThat(ex.getDesire().getDesiredType(),
                       equalTo((Class) String.class));
            assertThat(ex.getContext(), hasSize(2));
        }
    }

    /**
     * Test that the default on the alias works.
     */
    @Test
    public void testAliasDefault() throws InjectionException {
        InjectorBuilder bld = InjectorBuilder.create();
        Injector inj = bld.build();
        RequireQual rq = inj.getInstance(RequireQual.class);
        assertThat(rq.dependency, equalTo("wombat"));
        RequireAlias ra = inj.getInstance(RequireAlias.class);
        assertThat(ra.dependency, equalTo("wombat"));
    }

    /**
     * Basic qualifier
     */
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    @DefaultString("wombat")
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
