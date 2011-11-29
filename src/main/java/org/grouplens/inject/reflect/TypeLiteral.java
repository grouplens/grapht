package org.grouplens.inject.reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Class for expressing type literals. Create a concrete class extending
 * this class, instantiating <var>E</var> with a particular type, to express
 * that type statically.  This is pretty much like Guice's type literals.
 * <p/>
 * Example:
 * <pre>
 * {@code
 * TypeLiteral<List<String>> typ = new TypeLiteral<List<String>>() { }
 * }
 * </pre>
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 * @param <E> The type this literal is to express.
 */
public abstract class TypeLiteral<E> {
    private Type type;
    
    protected TypeLiteral() {
        type = extractType();
    }
    
    private Type extractType() {
        Type sc = getClass().getGenericSuperclass();
        try {
            ParameterizedType psc = (ParameterizedType) sc;
            if (!psc.getRawType().equals(TypeLiteral.class)) {
                throw new RuntimeException("TypeLiteral must be directly subclassed");
            }
            assert psc.getRawType().equals(TypeLiteral.class);
            return psc.getActualTypeArguments()[0];
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid subclassing", e);
        }
    }
    
	public Type getType() {
		return type;
	}
}
