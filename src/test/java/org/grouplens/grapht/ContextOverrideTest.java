package org.grouplens.grapht;

import org.grouplens.grapht.annotation.DefaultImplementation;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test using multiple context bindings to separate components.
 *
 * @author Michael Ekstrand
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
             .to(Plug.class);
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
}