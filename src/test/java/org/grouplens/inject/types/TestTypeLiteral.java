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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import org.grouplens.inject.types.TypeLiteral;
import org.junit.Test;


public class TestTypeLiteral {
	@Test
	public void testClass() {
		TypeLiteral<?> lit = new TypeLiteral<String>() { };
		Type type = lit.getType();
		assertEquals(String.class, type);
	}
	
	@Test
	public void testGenericFilled() {
	    TypeLiteral<?> lit = new TypeLiteral<List<String>>() { };
	    Type type = lit.getType();
	    assertThat(type, instanceOf(ParameterizedType.class));
	    ParameterizedType pt = (ParameterizedType) type;
	    assertThat(pt.getRawType(), equalTo((Type) List.class));
	    assertThat(pt.getActualTypeArguments()[0],
	               equalTo((Type) String.class));
	}
}
