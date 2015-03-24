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
