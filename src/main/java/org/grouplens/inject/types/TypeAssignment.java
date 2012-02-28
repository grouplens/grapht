package org.grouplens.inject.types;

import com.google.common.base.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An assignment of type variables to types. When applied to other types, it
 * fills them in according to this assignment.
 * 
 * @author Michael Ekstrand
 */
public class TypeAssignment implements Function<Type, Type> {
    @Nullable
    private final TypeAssignment base;
    @Nonnull
    private final Map<TypeVariable<?>,Type> map;

    /**
     * Construct a new, empty type assignment.
     */
    public TypeAssignment() {
        base = null;
        map = Collections.emptyMap();
    }

    /**
     * Construct a new type assignment from a map.
     * @param assignment The assignment of variables used by this assignment.
     */
    public TypeAssignment(@Nonnull Map<TypeVariable<?>,Type> assignment) {
        map = assignment;
        base = null;
    }

    /**
     * Construct a derived type assignment, delegating to the base assignment as
     * necessary.
     * 
     * @param base A type assignment to delegate to.
     * @param assignment The variable assignment for this assignment.
     */
    public TypeAssignment(@Nullable TypeAssignment base, @Nonnull Map<TypeVariable<?>,Type> assignment) {
        map = assignment;
        this.base = base;
    }
    
    /**
     * @return The backing map for this assignment
     */
    public Map<TypeVariable<?>, Type> getAssignment() {
        return Collections.unmodifiableMap(map);
    }

    /**
     * Apply this type assignment to a type. If the type is a variable, it is
     * substituted; if it is another kind of type, free variables are
     * substituted using this assignment.
     * 
     * @param type The type to substitute.
     * @return The type with variables substituted according to this assignment.
     */
    @Override
    public Type apply(Type type) {
        return Types.visit(type, new Visitor());
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TypeAssignment)) {
            return false;
        }
        TypeAssignment t = (TypeAssignment) o;
        return t.map.equals(map) && (base == null ? t.base == null : base.equals(t.base));
    }
    
    @Override
    public int hashCode() {
        return map.hashCode() ^ (base == null ? 0 : base.hashCode());
    }
    
    @Override
    public String toString() {
        return map.toString();
    }

    /**
     * Give this type assignment a new base assignment.
     * 
     * @param base The base assignment to use.
     * @return A type assignment with the same assignments as this one but a
     *         base/fallback assignment of <var>base</var>.
     */
    public TypeAssignment withBase(TypeAssignment base) {
        return new TypeAssignment(base, map);
    }

    private class Visitor extends TypeVisitor<Type> {
        @Override
        public Type visitDefault(Type type) {
            throw new IllegalArgumentException("invalid type for type assignment reference");
        }

        @Override
        public Type visitTypeVariable(TypeVariable<?> var) {
            Type type = map.get(var);
            if (base != null && type == null) {
                type = base.apply(var);
            }
            return type;
        }

        @Override
        public Type visitClass(Class<?> cls) {
            TypeVariable<?>[] vars = cls.getTypeParameters();
            final int n = vars.length;
            Type[] args = new Type[n];
            for (int i = 0; i < n; i++) {
                args[i] = apply(vars[i]);
                if (args[i] == null) {
                    throw new IllegalArgumentException("unassigned type variable " + vars[i].toString());
                }
            }
            return Types.parameterizedType(cls, args);
        }
    }

    /**
     * Create a type assignment from a parameterized type. The resulting
     * assignment assigns the formal parameters of the raw type to the actual
     * arguments in the parameterized type.
     * 
     * @param type The type from which to generate the type assignment.
     * @return A type assignment mapping formals to actuals.
     */
    public static TypeAssignment fromParameterizedType(ParameterizedType type) {
        Map<TypeVariable<?>,Type> typeMap = new HashMap<TypeVariable<?>,Type>();
        Class<?> base;
        try {
            base = (Class<?>) type.getRawType();
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("raw type is not a class", e);
        }
        TypeVariable<?>[] formals = base.getTypeParameters();
        Type[] actuals = type.getActualTypeArguments();
        if (formals.length != actuals.length) {
            String msg = String.format("wrong number of actual args (found %d, expected %d)",
                                       actuals.length, formals.length);
            throw new RuntimeException(msg);
        }
        for (int i = 0; i < actuals.length; i++) {
            typeMap.put(formals[i], actuals[i]);
        }
        return new TypeAssignment(typeMap);
    }
}
