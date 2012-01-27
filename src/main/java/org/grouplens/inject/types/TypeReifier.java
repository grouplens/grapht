package org.grouplens.inject.types;

import org.apache.commons.lang3.reflect.TypeUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;

/**
 * Logic for doing type reification. {@link Types#findCompatibleAssignment(Type, Type)}
 * is the entry point for this class — use it instead.
 * <p/>
 * Applying this visitor to a type yields another visitor which returns the assignment.
 * The reifier should be applied to the type for which the assignment is desired (α in
 * {@link Types#findCompatibleAssignment(Type, Type)}).
 *
 * @author Michael Ekstrand
 */
class TypeReifier extends TypeVisitor<TypeVisitor<TypeAssignment>> {
    private static TypeReifier instance = new TypeReifier();

    private static class ClassReifier extends TypeVisitor<TypeAssignment> {
        Class<?> classToMatch;

        public ClassReifier(Class<?> cls) {
            super(false);
            classToMatch = cls;
        }

        @Override
        public TypeAssignment visitParameterizedType(ParameterizedType type) {
            return new TypeAssignment(TypeUtils.determineTypeArguments(classToMatch, type));
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
     * @see Types#findCompatibleAssignment(Type, Type)
     */
    public static TypeAssignment makeCompatible(Type alpha, Type beta) {
        return instance.apply(alpha).apply(beta);
    }
}
