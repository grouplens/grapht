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
