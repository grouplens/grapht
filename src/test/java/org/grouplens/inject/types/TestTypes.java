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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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

    @Test
    public void testUnboundedWildcard() {
        TypeLiteral tlit = new TypeLiteral<Predicate<?>>() {};
        ParameterizedType ptype = (ParameterizedType) tlit.getType();
        WildcardType wildcard = (WildcardType) ptype.getActualTypeArguments()[0];
        WildcardType wcbld = Types.wildcardType();
        assertArrayEquals(wildcard.getUpperBounds(), wcbld.getUpperBounds());
        assertArrayEquals(wildcard.getLowerBounds(), wcbld.getLowerBounds());
        assertEquals(wildcard, wcbld);
        assertEquals(wildcard.hashCode(), wcbld.hashCode());
    }

    @Test
    public void testUpperBoundedWildcard() {
        TypeLiteral tlit = new TypeLiteral<Supplier<? extends Type>>() {};
        ParameterizedType ptype = (ParameterizedType) tlit.getType();
        WildcardType wildcard = (WildcardType) ptype.getActualTypeArguments()[0];
        WildcardType wcbld = Types.wildcardExtends(Type.class);
        assertArrayEquals(wildcard.getUpperBounds(), wcbld.getUpperBounds());
        assertArrayEquals(wildcard.getLowerBounds(), wcbld.getLowerBounds());
        assertEquals(wildcard, wcbld);
        assertEquals(wildcard.hashCode(), wcbld.hashCode());
    }

    @Test
    public void testLowerBoundedWildcard() {
        TypeLiteral tlit = new TypeLiteral<Predicate<? super InputStream>>() {};
        ParameterizedType ptype = (ParameterizedType) tlit.getType();
        WildcardType wildcard = (WildcardType) ptype.getActualTypeArguments()[0];
        WildcardType wcbld = Types.wildcardSuper(InputStream.class);
        assertArrayEquals(wildcard.getUpperBounds(), wcbld.getUpperBounds());
        assertArrayEquals(wildcard.getLowerBounds(), wcbld.getLowerBounds());
        assertEquals(wildcard, wcbld);
        assertEquals(wildcard.hashCode(), wcbld.hashCode());
    }

    @Test
    public void testParameterizedType() {
        TypeLiteral tlit = new TypeLiteral<Function<? super InputStream,? extends List<? extends String>>>() {};
        ParameterizedType ptype = (ParameterizedType) tlit.getType();
        ParameterizedType built =
                Types.parameterizedType(Function.class,
                                        Types.wildcardSuper(InputStream.class),
                                        Types.wildcardExtends(Types.parameterizedType(
                                                List.class, Types.wildcardExtends(String.class))));
        assertEquals(ptype.getRawType(), built.getRawType());
        assertArrayEquals(ptype.getActualTypeArguments(), built.getActualTypeArguments());
        assertEquals(ptype.getOwnerType(), built.getOwnerType());
        assertEquals(ptype, built);
        assertEquals(ptype.hashCode(), built.hashCode());
    }
}
