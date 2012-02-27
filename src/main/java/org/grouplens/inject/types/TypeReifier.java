package org.grouplens.inject.types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Logic for doing type reification.
 * {@link Types#findCompatibleAssignment(Type, Type)} is the entry point for
 * this class — use it instead.
 * <p/>
 * Applying this visitor to a type yields another visitor which returns the
 * assignment. The reifier should be applied to the type for which the
 * assignment is desired (α in
 * {@link Types#findCompatibleAssignment(Type, Type)}).
 * 
 * @author Michael Ekstrand
 */
class TypeReifier extends TypeVisitor<TypeVisitor<TypeAssignment>> {
    private static TypeReifier instance = new TypeReifier();

    private static class BoundsVerifier extends TypeVisitor<Boolean> {
        final TypeVariable<?> var;
        
        public BoundsVerifier(TypeVariable<?> var) {
            super(false);
            this.var = var;
        }
        
        @Override
        public Boolean visitClass(Class<?> t) {
            for (Type bound: var.getBounds()) {
                if (!Types.erase(bound).isAssignableFrom(t)) {
                    // the type is not assignable to one of the variable's bounds
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }
        
        @Override
        public Boolean visitParameterizedType(ParameterizedType t) {
            return visitClass(Types.erase(t));
        }
        
        @Override
        public Boolean visitWildcard(WildcardType t) {
            for (Type bound: var.getBounds()) {
                for (Type upper: t.getUpperBounds()) {
                    if (!Types.erase(upper).isAssignableFrom(Types.erase(bound))) {
                        return Boolean.FALSE;
                    }
                }
                for (Type lower: t.getLowerBounds()) {
                    if (!Types.erase(bound).isAssignableFrom(Types.erase(lower))) {
                        return Boolean.FALSE;
                    }
                }
            }
            return Boolean.TRUE;
        }
    }
    
    private static class ClassReifier extends TypeVisitor<TypeAssignment> {
        final Class<?> classToMatch;

        public ClassReifier(Class<?> cls) {
            super(false);
            classToMatch = cls;
        }

        @Override
        public TypeAssignment visitParameterizedType(ParameterizedType type) {
            // TypeUtils does not validate the bounds on TypeVariables, so we
            // have to check that later.
            Map<TypeVariable<?>, Type> assignment = TypeUtils.determineTypeArguments(classToMatch, type);
            if (assignment != null) {
                for (Entry<TypeVariable<?>, Type> e: assignment.entrySet()) {
                    boolean valid = new BoundsVerifier(e.getKey()).apply(e.getValue());
                    if (!valid) {
                        return null;
                    }
                }
                
                return new TypeAssignment(assignment);
            } else {
                return null;
            }
        }

        @Override
        public TypeAssignment visitClass(Class<?> cls) {
            if (cls.isAssignableFrom(classToMatch)) {
                return new TypeAssignment();
            } else {
                return null;
            }
        }
    }

    @Override
    public TypeVisitor<TypeAssignment> visitClass(Class<?> cls) {
        return new ClassReifier(cls);
    }

    /**
     * Find an assignment for α to make it compatible with β.
     * 
     * @see Types#findCompatibleAssignment(Type, Type)
     */
    public static TypeAssignment makeCompatible(Type alpha, Type beta) {
        return instance.apply(alpha).apply(beta);
    }
}
