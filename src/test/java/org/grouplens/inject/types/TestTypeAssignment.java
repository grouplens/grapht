package org.grouplens.inject.types;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.google.common.base.Function;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.inject.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Test
    public void testApplyToClass() {
        TypeLiteral<?> tlit = new TypeLiteral<List<String>>() {};
        Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();
        map.put(List.class.getTypeParameters()[0], String.class);
        TypeAssignment asn = new TypeAssignment(map);
        Type out = asn.apply(List.class);
        assertThat(out, equalTo(tlit.getType()));
    }

    static interface FooProvider<T> extends Provider<T> {}

    @Test
    public void testBoundWildcardBinding() {
        // simulate a binding matching on a bound wildcard, make sure all
        // the parts work.
        Type matcher = Types.parameterizedType(Provider.class,
                                               Types.wildcardExtends(Node.class));
        Type target = FooProvider.class;
        Type desire = Types.parameterizedType(Provider.class, Types.wildcardExtends(Element.class));
        Type expectedNewDesire = Types.parameterizedType(FooProvider.class,
                                                         Types.wildcardExtends(Element.class));
        
        // bind rule validity checking should work
        assertTrue(TypeUtils.isAssignable(target, matcher));

        // generating the new desire should work
        TypeAssignment asn = Types.findCompatibleAssignment(target, desire);
        Type newDesire = asn.apply(target);
        assertThat(newDesire, equalTo(expectedNewDesire));
    }
}
