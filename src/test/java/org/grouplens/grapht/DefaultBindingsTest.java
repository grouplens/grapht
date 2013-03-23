package org.grouplens.grapht;

import org.grouplens.grapht.types.dft.*;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Acceptance tests for default bindings (via annotations and such). This may well have
 * overlap with some other test cases, but consolidates various expected default behavior in one
 * place.
 */
public class DefaultBindingsTest {
    private InjectorBuilder b;
    // TODO Add tests here for DefaultInt and friends

    @Before
    public void setup() {
        b = new InjectorBuilder();
    }

    @Test
    public void testDefaultImplementation() {
        Injector inj = b.build();
        IDftImpl a = inj.getInstance(IDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CDftImplA.class));
    }

    @Test
    public void testOverrideDefaultImplementation() {
        b.bind(IDftImpl.class).to(CDftImplB.class);
        Injector inj = b.build();
        IDftImpl a = inj.getInstance(IDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CDftImplB.class));
    }

    @Test
    public void testDefaultProvider() {
        Injector inj = b.build();
        IDftProvider a = inj.getInstance(IDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(PDftProvider.Impl.class));
    }

    @Test
    public void testOverrideDefaultProvider() {
        b.bind(IDftProvider.class).to(CDftProvider.class);
        Injector inj = b.build();
        IDftProvider a = inj.getInstance(IDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CDftProvider.class));
    }

    @Test
    public void testPropDefaultImplementation() {
        Injector inj = b.build();
        IPropDftImpl a = inj.getInstance(IPropDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPropDftImplA.class));
    }

    @Test
    public void testPropOverrideDefaultImplementation() {
        b.bind(IPropDftImpl.class).to(CPropDftImplB.class);
        Injector inj = b.build();
        IPropDftImpl a = inj.getInstance(IPropDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPropDftImplB.class));
    }

    @Test
    public void testPropDefaultProvider() {
        Injector inj = b.build();
        IPropDftProvider a = inj.getInstance(IPropDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(PPropDftProvider.Impl.class));
    }

    @Test
    public void testPropOverrideDefaultProvider() {
        b.bind(IPropDftProvider.class).to(CPropDftProvider.class);
        Injector inj = b.build();
        IPropDftProvider a = inj.getInstance(IPropDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPropDftProvider.class));
    }
}
