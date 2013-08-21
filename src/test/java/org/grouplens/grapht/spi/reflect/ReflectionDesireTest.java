/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
package org.grouplens.grapht.spi.reflect;

import org.junit.Assert;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.solver.BindingResult;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.InjectionContext;
import org.grouplens.grapht.solver.SolverException;
import org.grouplens.grapht.spi.Attributes;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.spi.MockInjectionPoint;
import org.grouplens.grapht.spi.reflect.types.*;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectionDesireTest {
    private static <T extends Annotation> Attributes qualifier(Class<T> qtype) {
        return new AttributesImpl(new Annotation[] { AnnotationBuilder.of(qtype).build() });
    }
    
    @Test
    public void testSubtypeInjectionPointSatisfactionConstructor() throws Exception {
        ClassSatisfaction satis = new ClassSatisfaction(B.class);
        InjectionPoint inject = new MockInjectionPoint(A.class, false);
        ReflectionDesire desire = new ReflectionDesire(B.class, inject, satis);
        
        Assert.assertEquals(B.class, desire.getDesiredType());
        Assert.assertEquals(satis, desire.getSatisfaction());
        Assert.assertEquals(inject, desire.getInjectionPoint());
    }
    
    @Test
    public void testInheritedRoleDefault() throws Exception {
        // Test that the default desire for the setRoleE injection point in TypeC
        // defaults to TypeB.  This also tests qualifier default inheritence
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(TypeC.class);
        ReflectionDesire dflt = getDefaultDesire(TypeC.class.getMethod("setRoleD", InterfaceB.class), desires);
        
        Assert.assertTrue(dflt.getSatisfaction() instanceof ClassSatisfaction);
        Assert.assertEquals(qualifier(RoleD.class), dflt.getInjectionPoint().getAttributes());
        Assert.assertEquals(TypeB.class, ((ClassSatisfaction) dflt.getSatisfaction()).getErasedType());
        Assert.assertEquals(TypeB.class, dflt.getDesiredType());
    }
    
    @Test
    public void testRoleParameterDefault() throws Exception {
        // Test that the default desire for the constructor injection in TypeC
        // defaults to the int value 5
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(TypeC.class);
        ReflectionDesire dflt = getDefaultDesire(0, desires);
        
        Assert.assertTrue(dflt.getSatisfaction() instanceof InstanceSatisfaction);
        Assert.assertEquals(qualifier(ParameterA.class), dflt.getInjectionPoint().getAttributes());
        Assert.assertEquals(Integer.class, dflt.getDesiredType());
        Assert.assertEquals(5, ((InstanceSatisfaction) dflt.getSatisfaction()).getInstance());
    }
    
    @Test
    public void testProvidedByDefault() throws Exception {
        // Test that the default desire for the setTypeA injection point in TypeC
        // is satisfied by a provider satisfaction to ProviderA
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(TypeC.class);
        ReflectionDesire dflt = getDefaultDesire(TypeC.class.getMethod("setTypeA", TypeA.class), desires);
        
        Assert.assertTrue(dflt.getSatisfaction() instanceof ProviderClassSatisfaction);
        Assert.assertNull(dflt.getInjectionPoint().getAttributes().getQualifier());
        Assert.assertEquals(TypeA.class, dflt.getDesiredType());
        Assert.assertEquals(ProviderA.class, ((ProviderClassSatisfaction) dflt.getSatisfaction()).getProviderType());
    }
    
    @Test
    public void testImplementedByDefault() throws Exception {
        // Test that the default desire for the setRoleA injection point in TypeC
        // is satisfied by a type binding to TypeA
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(TypeC.class);
        ReflectionDesire dflt = getDefaultDesire(TypeC.class.getMethod("setRoleA", InterfaceA.class), desires);
        
        Assert.assertTrue(dflt.getSatisfaction() instanceof ClassSatisfaction);
        Assert.assertEquals(qualifier(RoleA.class), dflt.getInjectionPoint().getAttributes());
        Assert.assertEquals(TypeA.class, ((ClassSatisfaction) dflt.getSatisfaction()).getErasedType());
        Assert.assertEquals(TypeA.class, dflt.getDesiredType());
    }
    
    @Test
    public void testNoDefaultDesire() throws Exception {
        // Test that there is no default desire for the setTypeB injection point
        // in TypeC, but that it is still satisfiable
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(TypeC.class);
        ReflectionDesire dflt = getDefaultDesire(TypeC.class.getMethod("setTypeB", TypeB.class), desires);
        
        Assert.assertNull(dflt);
    }

    /**
     * If we have a nullable injection point, and restrict the desire to a class, we
     * should throw out the null satisfaction (and recompute it, if appropriate) based
     * on the restricted type.
     */
    @Test
    public void testRestrictNullableDesire() throws NoSuchMethodException, SolverException {
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(ReqB.class);
        Assert.assertEquals(1, desires.size());
        ReflectionDesire desire = desires.get(0);
        Desire restricted = desire.restrict(TypeB.class);
        Assert.assertNotNull(restricted);
    }
    
    private ReflectionDesire getDefaultDesire(Object methodOrCtorParam, List<ReflectionDesire>  desires) throws SolverException {
        BindingResult result = null;
        for (ReflectionDesire d: desires) {
            if (methodOrCtorParam instanceof Method) {
                if (d.getInjectionPoint() instanceof SetterInjectionPoint) {
                    SetterInjectionPoint sp = (SetterInjectionPoint) (d.getInjectionPoint());
                    if (sp.getMember().equals(methodOrCtorParam)) {
                        result = new DefaultDesireBindingFunction(new ReflectionInjectSPI()).bind(InjectionContext.empty(), d);
                        break;
                    }
                }
            } else { // assume its an Integer
                if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                    ConstructorParameterInjectionPoint cp = (ConstructorParameterInjectionPoint) (d.getInjectionPoint());
                    if (((Integer) methodOrCtorParam).intValue() == cp.getParameterIndex()) {
                        result = new DefaultDesireBindingFunction(new ReflectionInjectSPI()).bind(InjectionContext.empty(), d);
                        break;
                    }
                }
            }
        }
        
        return (result == null ? null : (ReflectionDesire) result.getDesire());
    }
    
    public static class A { }
    
    public static class B extends A { }
    
    public static class C { }

    public static class ReqB {
        @Inject
        public void setB(@Nullable InterfaceB foo) {
            /* do nothing */
        }
    }
}
