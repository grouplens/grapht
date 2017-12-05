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
import org.grouplens.grapht.annotation.DefaultProvider;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class InjectionPointDefaultTest {
    /**
     * Basic test to make sure the standard defaulting stil works.
     */
    @Test
    public void usesDefaultImplementationFromInterface() throws InjectionException {
        InjectorBuilder ijb = InjectorBuilder.create();
        Injector inj = ijb.build();
        BareDep bd = inj.getInstance(BareDep.class);
        assertThat(bd, notNullValue());
        assertThat(bd.iface, instanceOf(ImplA.class));
    }

    /**
     * Do we use the default implementation from the injection point?
     */
    @Test
    public void usesDefaultImplementationFromIP() throws InjectionException {
        InjectorBuilder ijb = InjectorBuilder.create();
        Injector inj = ijb.build();
        DefImplDep obj = inj.getInstance(DefImplDep.class);
        assertThat(obj, notNullValue());
        assertThat(obj.iface, instanceOf(ImplB.class));
    }

    /**
     * Do we use the default provider from the injection point?
     */
    @Test
    public void usesDefaultProviderFromIP() throws InjectionException {
        InjectorBuilder ijb = InjectorBuilder.create();
        Injector inj = ijb.build();
        DefProvDep obj = inj.getInstance(DefProvDep.class);
        assertThat(obj, notNullValue());
        assertThat(obj.iface, instanceOf(ImplP.class));
    }

    /**
     * Does a binding override a default implementation?
     */
    @Test
    public void usesBindingOverridesIPDefaultImplementation() throws InjectionException {
        InjectorBuilder ijb = InjectorBuilder.create();
        ijb.bind(Iface.class).to(ImplC.class);
        Injector inj = ijb.build();
        DefImplDep obj = inj.getInstance(DefImplDep.class);
        assertThat(obj, notNullValue());
        assertThat(obj.iface, instanceOf(ImplC.class));
    }

    /**
     * Does a binding override a default provider?
     */
    @Test
    public void usesBindingOverridesIPDefaultProvider() throws InjectionException {
        InjectorBuilder ijb = InjectorBuilder.create();
        ijb.bind(Iface.class).to(ImplC.class);
        Injector inj = ijb.build();
        DefProvDep obj = inj.getInstance(DefProvDep.class);
        assertThat(obj, notNullValue());
        assertThat(obj.iface, instanceOf(ImplC.class));
    }

    public static class BareDep {
        private final Iface iface;

        @Inject
        public BareDep(Iface i) {
            iface = i;
        }
    }

    public static class DefImplDep {
        private final Iface iface;

        @Inject
        public DefImplDep(@DefaultImplementation(ImplB.class) Iface i) {
            iface = i;
        }
    }

    public static class DefProvDep {
        private final Iface iface;

        @Inject
        public DefProvDep(@DefaultProvider(IfaceProvider.class) Iface i) {
            iface = i;
        }
    }

    @DefaultImplementation(ImplA.class)
    public static interface Iface {
        default String getImplName() {
            return getClass().getSimpleName();
        }
    }

    public static class ImplA implements Iface {}

    public static class ImplB implements Iface {}

    public static class ImplC implements Iface {}

    public static class ImplP implements Iface {
        private final String name;

        public ImplP(String n) {
            name = n;
        }
        public String getImplName() {
            return name;
        }
    }

    public static class IfaceProvider implements Provider<Iface> {
        @Override
        public Iface get() {
            return new ImplP("provided");
        }
    }
}
