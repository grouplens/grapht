package org.grouplens.inject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashSet;
import java.util.Set;

public class TypeTest {
    
    public static void main(String[] args) throws Exception {
        printType("method1", TypeTest.class.getMethod("method1", IA.class).getGenericParameterTypes()[0]);
        printType("method2", TypeTest.class.getMethod("method2", A.class).getGenericParameterTypes()[0]);
        printType("method3", TypeTest.class.getMethod("method3", A.class).getGenericParameterTypes()[0]);
        printType("method4", TypeTest.class.getMethod("method4", A.class).getGenericParameterTypes()[0]);

        printType("Interface IA", IA.class);
        printType("Anonymous IA", new IA<Integer>() { }.getClass());
        printType("Static A", A.class);
        printType("Instance A", new A<Integer>().getClass());
        printType("Static B", B.class);
    }
    
    public static void printType(String name, Type t) throws Exception {
        printType(t, "", name + ": ", new HashSet<Type>());
    }
    
    public static void printType(Type t, String prefix, String label, Set<Type> visited) throws Exception {
        if (visited.contains(t))
            return;
        
        visited.add(t);
        if (t instanceof Class) {
            Class<?> c = (Class<?>) t;
            System.out.println(prefix + label + " class: " + c.getName());
            if (c.getSuperclass() != null)
                printType(c.getGenericSuperclass(), prefix + "  ", "Superclass: ", visited);
            for (Type i: c.getGenericInterfaces())
                printType(i, prefix + "  ", "Implements: ", visited);
            for (Type p: c.getTypeParameters())
                printType(p, prefix + "  ", "Parameters: ", visited);
        } else if (t instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) t;
            System.out.println(prefix + label + " parameterized-type: ");
            printType(p.getRawType(), prefix + "  ", "Raw: ", visited);
            for (Type a: p.getActualTypeArguments())
                printType(a, prefix + "  ", "Parameter: ", visited);
        } else if (t instanceof TypeVariable) {
            TypeVariable<?> v = (TypeVariable<?>) t;
            System.out.println(prefix + label + " variable: " + v.getName());
            for (Type b: v.getBounds())
                printType(b, prefix + "  ", "Bounds: ", visited);
        } else if (t instanceof WildcardType) {
            WildcardType w = (WildcardType) t;
            System.out.println(prefix + label + " wildcard: ");
            for (Type u: w.getUpperBounds())
                printType(u, prefix + "  ", "Upper Bounds: ", visited);
            for (Type l: w.getLowerBounds())
                printType(l, prefix + "  ", "Lower Bounds: ", visited);
        }
        visited.remove(t);
    }

    public static void method1(IA<String> ia) { }
    
    public static void method2(A<? extends Integer> a) { }
    
    public static void method3(A<? super Number> a) { }
    
    public static <T extends Number> void method4(A<? super T> a) { }
    
    public static interface IA<T> { }
    
    public static class A<X extends Number> implements IA<X> { }
    
    public static class B extends A<Double> { }
}
