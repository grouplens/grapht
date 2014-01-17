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

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.grouplens.grapht.context.ContextElements;
import org.grouplens.grapht.context.ContextPattern;
import org.grouplens.grapht.context.Multiplicity;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

/**
 * Test using multiple context bindings to separate components.
 *
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ContextOverrideTest {
    private InjectorBuilder build;

    @Before
    public void setup() {
        build = new InjectorBuilder();
    }

    /**
     * Test bindings with a null, and a deeper context using a binding. To make things
     * interesting, the thing tweaked is a concrete class.
     */
    @Test
    public void testDeeperOverride() {
        build.in(Outer.class)
             .bind(IPlug.class)
             .to(PlugH.class);
        build.in(Inner.class)
             .bind(IPlug.class)
             .to(PlugA.class);
        Injector inj = build.build();

        Inner in = inj.getInstance(Inner.class);
        assertThat(in, notNullValue());
        assertThat(in.plug, instanceOf(PlugA.class));

        Outer out = inj.getInstance(Outer.class);
        assertThat(out, notNullValue());
        assertThat(out.plug, instanceOf(PlugH.class));
        assertThat(out.inner, notNullValue());
        assertThat(out.inner.plug, instanceOf(PlugA.class));
    }

    /**
     * Test bindings with a null, and a deeper context using a binding.
     */
    @Test
    public void testNullDeepConcrete() {
        // this can be with or without the context
        // use context to match target use in LensKit
        build.in(COuter.class)
             .bind(Plug.class)
             .toNull();
        build.in(CInner.class)
             .bind(Plug.class)
             .to(Plug.class, false);
        Injector inj = build.build();

        CInner in = inj.getInstance(CInner.class);
        assertThat(in, notNullValue());
        assertThat(in.plug, notNullValue());

        COuter out = inj.getInstance(COuter.class);
        assertThat(out, notNullValue());
        assertThat(out.plug, nullValue());
        assertThat(out.inner, notNullValue());
        assertThat(out.inner.plug, notNullValue());
        assertThat(out.inner.plug, instanceOf(Plug.class));
    }

    /**
     * Test bindings with a null, and a deeper context using a binding, with
     * an anchored matcher.
     */
    @Test
    public void testNullDeepConcreteAnchored() {
        // immediate binding should allow inner to get a plug
        build.at(COuter.class)
             .bind(Plug.class)
             .toNull();
        Injector inj = build.build();

        CInner in = inj.getInstance(CInner.class);
        assertThat(in, notNullValue());
        assertThat(in.plug, notNullValue());

        COuter out = inj.getInstance(COuter.class);
        assertThat(out, notNullValue());
        assertThat(out.plug, nullValue());
        assertThat(out.inner, notNullValue());
        assertThat(out.inner.plug, notNullValue());
        assertThat(out.inner.plug, instanceOf(Plug.class));
    }

    /**
     * Test anchored root binding.
     */
    @Test
    public void testAnchoredToRoot() {
        build.bind(IPlug.class)
             .to(PlugA.class);
        build.at(null)
             .bind(IPlug.class)
             .to(PlugH.class);
        Injector inj = build.build();

        // Does directly requesting a plug get us the anchored binding?
        assertThat(inj.getInstance(IPlug.class),
                   instanceOf(PlugH.class));

        // Is the non-anchored binding used for dependencies?
        Outer out = inj.getInstance(Outer.class);
        assertThat(out.plug,
                   instanceOf(PlugA.class));
        assertThat(out.inner.plug,
                   instanceOf(PlugA.class));
        assertThat(out.plug, sameInstance(out.inner.plug));

        // quick check this again, make sure nothing changed
        assertThat(inj.getInstance(IPlug.class),
                   instanceOf(PlugH.class));
    }

    @Test
    public void testPatternForPlug() {
        build.matching(ContextPattern.any()
                                     .append(CInner.class)
                                     .append(ContextElements.invertMatch(ContextElements.matchType(PlugW.class)),
                                             Multiplicity.ZERO_OR_MORE))
             .bind(Plug.class)
             .to(PlugW.class);
        Injector inj = build.build();
        CInner c = inj.getInstance(CInner.class);
        assertThat(c.plug, instanceOf(PlugW.class));
        assert c.plug != null;
        assertThat(((PlugW) c.plug).inner.getClass(),
                   equalTo((Class) Plug.class));
    }

    @DefaultImplementation(PlugA.class)
    public static interface IPlug {}
    public static class PlugA implements IPlug {}
    public static class PlugH implements IPlug {}

    public static class Inner {
        final IPlug plug;

        @Inject
        public Inner(IPlug p) {
            plug = p;
        }
    }

    public static class Outer {
        final IPlug plug;
        final Inner inner;

        @Inject
        public Outer(Inner in, @Nullable IPlug p) {
            plug = p;
            inner = in;
        }
    }

    public static class Plug {}

    public static class CInner {
        final Plug plug;

        @Inject
        public CInner(@Nullable Plug p) {
            plug = p;
        }
    }

    public static class COuter {
        final Plug plug;
        final CInner inner;

        @Inject
        public COuter(CInner in, @Nullable Plug p) {
            plug = p;
            inner = in;
        }
    }

    public static class PlugW extends Plug {
        private final Plug inner;

        @Inject
        public PlugW(Plug wrapped) {
            inner = wrapped;
        }
    }
}
