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

import org.grouplens.grapht.types.dft.*;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
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
        b = InjectorBuilder.create(DefaultBindingsTest.class.getClassLoader());
    }

    @Test
    public void testDefaultImplementation() throws InjectionException {
        Injector inj = b.build();
        IDftImpl a = inj.getInstance(IDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CDftImplA.class));
    }

    @Test
    public void testOverrideDefaultImplementation() throws InjectionException {
        b.bind(IDftImpl.class).to(CDftImplB.class);
        Injector inj = b.build();
        IDftImpl a = inj.getInstance(IDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CDftImplB.class));
    }

    @Test
    public void testDefaultProvider() throws InjectionException {
        Injector inj = b.build();
        IDftProvider a = inj.getInstance(IDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(PDftProvider.Impl.class));
    }

    @Test
    public void testOverrideDefaultProvider() throws InjectionException {
        b.bind(IDftProvider.class).to(CDftProvider.class);
        Injector inj = b.build();
        IDftProvider a = inj.getInstance(IDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CDftProvider.class));
    }

    @Test
    public void testPropDefaultImplementation() throws InjectionException {
        Injector inj = b.build();
        IPropDftImpl a = inj.getInstance(IPropDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPropDftImplA.class));
    }

    @Test
    public void testPropOverrideDefaultImplementation() throws InjectionException {
        b.bind(IPropDftImpl.class).to(CPropDftImplB.class);
        Injector inj = b.build();
        IPropDftImpl a = inj.getInstance(IPropDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPropDftImplB.class));
    }

    @Test
    public void testPropDefaultProvider() throws InjectionException {
        Injector inj = b.build();
        IPropDftProvider a = inj.getInstance(IPropDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(PPropDftProvider.Impl.class));
    }

    @Test
    public void testPropOverrideDefaultProvider() throws InjectionException {
        b.bind(IPropDftProvider.class).to(CPropDftProvider.class);
        Injector inj = b.build();
        IPropDftProvider a = inj.getInstance(IPropDftProvider.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPropDftProvider.class));
    }

    @Test
    public void testPropImplDoubleDepCache() throws InjectionException {
        Injector inj = b.build();
        CPropImplDoubleDep obj = inj.getInstance(CPropImplDoubleDep.class);
        assertThat(obj.right, sameInstance(obj.left));
    }

    @Test
    public void testPropImplDoubleDepUncache() throws InjectionException {
        b.setDefaultCachePolicy(CachePolicy.NEW_INSTANCE);
        Injector inj = b.build();
        CPropImplDoubleDep obj = inj.getInstance(CPropImplDoubleDep.class);
        assertThat(obj.right, not(sameInstance(obj.left)));
    }

    @Test
    public void testPropImplDoubleDepPropUncache() throws InjectionException {
        Injector inj = b.build();
        CPropImplDoubleDepNoCache obj = inj.getInstance(CPropImplDoubleDepNoCache.class);
        assertThat(obj.right, not(sameInstance(obj.left)));
    }

    @Test
    public void testDftImplDoubleDepCache() throws InjectionException {
        Injector inj = b.build();
        CDoubleDep obj = inj.getInstance(CDoubleDep.class);
        assertThat(obj.right, sameInstance(obj.left));
    }

    @Test
    public void testDftImplDoubleDepUncache() throws InjectionException {
        b.setDefaultCachePolicy(CachePolicy.NEW_INSTANCE);
        Injector inj = b.build();
        CDoubleDep obj = inj.getInstance(CDoubleDep.class);
        assertThat(obj.right, not(sameInstance(obj.left)));
    }

    @Test
    public void testDftImplDoubleDepPropUncache() throws InjectionException {
        Injector inj = b.build();
        CDoubleDepNoCache obj = inj.getInstance(CDoubleDepNoCache.class);
        assertThat(obj.right, not(sameInstance(obj.left)));
    }

    /**
     * Test that the DefaultImplementation annotation overrides META-INF.
     */
    @Test
    public void testPreemptDefaultImplementation() throws InjectionException {
        Injector inj = b.build();
        IPreemptDftImpl a = inj.getInstance(IPreemptDftImpl.class);
        assertThat(a, notNullValue());
        assertThat(a, instanceOf(CPreemptDftImplA.class));
    }
}
