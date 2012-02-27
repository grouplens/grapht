package org.grouplens.inject.types;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import com.google.common.base.Function;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Ekstrand
 */
public class TestTypeReifier {
    static interface WombatFunction<T> extends Function<InputStream,T> {}

    @Test
    public void testReifyClassToSelf() {
        TypeAssignment asn = Types.findCompatibleAssignment(List.class, List.class);
        assertThat(asn, notNullValue());
    }

    @Test
    public void testReifyClassToSuper() {
        TypeAssignment asn = Types.findCompatibleAssignment(ArrayList.class, List.class);
        assertThat(asn, notNullValue());
    }

    @Test
    public void testReifyClassToSub() {
        TypeAssignment asn = Types.findCompatibleAssignment(List.class, ArrayList.class);
        assertThat(asn, nullValue());
    }

    @Test
    public void testReifyClassToOther() {
        TypeAssignment asn = Types.findCompatibleAssignment(ArrayList.class, InputStream.class);
        assertThat(asn, nullValue());
    }

    @Test
    public void testReifyImplementation() {
        TypeLiteral strlit = new TypeLiteral<List<String>>() {};
        TypeAssignment asn = Types.findCompatibleAssignment(ArrayList.class, strlit.getType());
        assertThat(asn, notNullValue());
        assertThat(asn.apply(ArrayList.class),
                   equalTo((Type) Types.parameterizedType(ArrayList.class, String.class)));
    }

    @Test
    public void testReifyWildcard() {
        Type desire = Types.parameterizedType(Function.class,
                                              Types.wildcardSuper(InputStream.class),
                                              Types.wildcardExtends(String.class));
        TypeAssignment asn =
                Types.findCompatibleAssignment(WombatFunction.class, desire);
        assertThat(asn, notNullValue());
        Type newDesire = Types.parameterizedType(WombatFunction.class,
                                                 Types.wildcardExtends(String.class));
        assertThat(asn.apply(WombatFunction.class),
                   equalTo(newDesire));
    }
}
