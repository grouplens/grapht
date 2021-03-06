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
        build = InjectorBuilder.create();
    }

    /**
     * Test bindings with a null, and a deeper context using a binding. To make things
     * interesting, the thing tweaked is a concrete class.
     */
    @Test
    public void testDeeperOverride() throws InjectionException {
        build.within(Outer.class)
             .bind(IPlug.class)
             .to(PlugH.class);
        build.within(Inner.class)
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
    public void testNullDeepConcrete() throws InjectionException {
        // this can be with or without the context
        // use context to match target use in LensKit
        build.within(COuter.class)
             .bind(Plug.class)
             .toNull();
        build.within(CInner.class)
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
    public void testNullDeepConcreteAnchored() throws InjectionException {
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
    public void testAnchoredToRoot() throws InjectionException {
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
    public void testPatternForPlug() throws InjectionException {
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
