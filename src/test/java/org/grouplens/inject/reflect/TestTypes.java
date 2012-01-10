/*
 * LensKit, a reference implementation of recommender algorithms.
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
package org.grouplens.inject.reflect;

import org.grouplens.inject.spi.reflect.TypeLiteral;
import org.grouplens.inject.spi.reflect.Types;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.*;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@SuppressWarnings("rawtypes")
public class TestTypes {

    @Test
    public void testEraseClass() {
        assertThat(Types.erase(String.class),
                equalTo((Class) String.class));
    }

    @Test
    public void testEraseParamType() {
        TypeLiteral<List<String>> tl = new TypeLiteral<List<String>>() {};
        assertThat(Types.erase(tl.getType()), equalTo((Class) List.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEraseVariable() {
        Type var = List.class.getTypeParameters()[0];
        Types.erase(var);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEraseWildcard() throws NoSuchFieldException {
        class WildTest {
            @SuppressWarnings("unused")
            public List<? extends InputStream> field;
        }

        Field field = WildTest.class.getField("field");
        Type ft = field.getGenericType();
        assertThat(Types.erase(ft), equalTo((Class) List.class));
        ParameterizedType pft = (ParameterizedType) ft;
        Type param = pft.getActualTypeArguments()[0];
        assertThat(param, CoreMatchers.<Object>instanceOf(WildcardType.class));
        // finally, throw an illegal argument exception
        Types.erase(param);
    }
}
