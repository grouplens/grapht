/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.types;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.google.common.base.Function;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author Michael Ekstrand
 */
public class TestTypeAssignment {
    @Test
    public void testExtractFromParameterizedType() throws Exception {
        Type type = new TypeLiteral<Function<String,Integer>>() {}.getType();
        TypeAssignment assign = TypeAssignment.fromParameterizedType((ParameterizedType) type);
        assertThat(assign, notNullValue());
        assertThat(assign.apply(Function.class.getTypeParameters()[0]),
                   equalTo((Type) String.class));
        assertThat(assign.apply(Function.class.getTypeParameters()[1]),
                   equalTo((Type) Integer.class));
    }

    @Test
    public void testExtractFromComplexPType() throws Exception {
        Type type = new TypeLiteral<Function<? super List<String>,Integer>>() {}.getType();
        TypeAssignment assign = TypeAssignment.fromParameterizedType((ParameterizedType) type);
        assertThat(assign, notNullValue());

        Type lStr = new TypeLiteral<List<String>>() {}.getType();
        Type wild = Types.wildcardSuper(lStr);
        assertThat(assign.apply(Function.class.getTypeParameters()[0]),
                   equalTo(wild));

        assertThat(assign.apply(Function.class.getTypeParameters()[1]),
                   equalTo((Type) Integer.class));
    }
}
