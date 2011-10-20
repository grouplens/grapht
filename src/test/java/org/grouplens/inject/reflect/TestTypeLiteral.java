package org.grouplens.inject.reflect;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

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
