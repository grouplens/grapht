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

import org.grouplens.grapht.reflect.internal.types.TypeDftN;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class NullComponentTest {
    @Test
    public void testDefaultNull() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        Injector inj = b.build();
        NullableDep obj = inj.getInstance(NullableDep.class);
        assertThat(obj, not(nullValue()));
        assertThat(obj.getDep(), nullValue());
    }

    @Test
    public void testDefaultAbsent() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        Injector inj = b.build();
        OptionalDep obj = inj.getInstance(OptionalDep.class);
        assertThat(obj, not(nullValue()));
        assertThat(obj.getDep(), nullValue());
    }

    @Test(expected = ConstructionException.class)
    public void testBadNull() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        Injector inj = b.build();
        inj.getInstance(RequireDep.class);
    }

    private static class NullableDep {
        private TypeDftN depend;
        @Inject
        public NullableDep(@Nullable TypeDftN dep) {
            depend = dep;
        }
        public TypeDftN getDep() {
            return depend;
        }
    }

    private static class OptionalDep {
        private TypeDftN depend;
        @Inject
        public OptionalDep(Optional<TypeDftN> dep) {
            depend = dep.orElse(null);
        }
        public TypeDftN getDep() {
            return depend;
        }
    }

    private static class RequireDep {
        @Inject
        public RequireDep(TypeDftN dep) {
        }
    }
}
