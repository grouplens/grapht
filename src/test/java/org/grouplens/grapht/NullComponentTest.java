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
