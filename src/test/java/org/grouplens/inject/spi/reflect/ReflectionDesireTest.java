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
package org.grouplens.inject.spi.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.Assert;

import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.reflect.ReflectionDesire.DefaultSource;
import org.grouplens.inject.spi.reflect.types.InterfaceA;
import org.grouplens.inject.spi.reflect.types.InterfaceB;
import org.grouplens.inject.spi.reflect.types.ParameterA;
import org.grouplens.inject.spi.reflect.types.ProviderA;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleB;
import org.grouplens.inject.spi.reflect.types.RoleE;
import org.grouplens.inject.spi.reflect.types.TypeA;
import org.grouplens.inject.spi.reflect.types.TypeB;
import org.grouplens.inject.spi.reflect.types.TypeC;
import org.junit.Test;

public class ReflectionDesireTest {
    @Test
    public void testInjectionPointConstructor() throws Exception {
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(A.class, null, false, false));
        Assert.assertEquals(A.class, desire.getDesiredType());
        Assert.assertFalse(desire.isTransient());
        Assert.assertNull(desire.getQualifier());
        
        desire = new ReflectionDesire(new MockInjectionPoint(A.class, new AnnotationQualifier(ParameterA.class), true, false));
        Assert.assertEquals(A.class, desire.getDesiredType());
        Assert.assertTrue(desire.isTransient());
        Assert.assertEquals(new AnnotationQualifier(ParameterA.class), desire.getQualifier());
    }
    
    @Test
    public void testSubtypeInjectionPointSatisfactionConstructor() throws Exception {
        ClassSatisfaction satis = new ClassSatisfaction(B.class);
        InjectionPoint inject = new MockInjectionPoint(A.class, null, false, false);
        ReflectionDesire desire = new ReflectionDesire(B.class, inject, satis, DefaultSource.QUALIFIER_AND_TYPE);
        
        Assert.assertEquals(B.class, desire.getDesiredType());
        Assert.assertEquals(satis, desire.getSatisfaction());
        Assert.assertEquals(inject, desire.getInjectionPoint());
    }
    
    @Test
    public void testInheritedRoleDefault() throws Exception {
        // Test that the default desire for the setRoleE injection point in TypeC
        // defaults to TypeB.  This also tests qualifier default inheritence
        List<ReflectionDesire> desires = ReflectionDesire.getDesires(TypeC.class);
        ReflectionDesire dflt = getDefaultDesire(TypeC.class.getMethod("setRoleE", InterfaceB.class), desires);
        
        Assert.assertTrue(dflt.getSatisfaction() instanceof ClassSatisfaction);
        Assert.assertEquals(new AnnotationQualifier(RoleE.class), dflt.getQualifier());
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
        Assert.assertEquals(new AnnotationQualifier(ParameterA.class), dflt.getQualifier());
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
        Assert.assertNull(dflt.getQualifier());
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
        Assert.assertEquals(new AnnotationQualifier(RoleA.class), dflt.getQualifier());
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
    
    @Test
    public void testBindRuleComparator() throws Exception {
        BindRule b1 = new ReflectionBindRule(InterfaceB.class, new InstanceSatisfaction(new TypeB()), new AnnotationQualifier(RoleB.class), 0, true);
        BindRule b2 = new ReflectionBindRule(InterfaceB.class, new InstanceSatisfaction(new TypeB()), new AnnotationQualifier(RoleA.class), 0, true);
        BindRule b3 = new ReflectionBindRule(InterfaceB.class, new InstanceSatisfaction(new TypeB()), new AnnotationQualifier(RoleA.class), 1, true);
        
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(InterfaceB.class, new AnnotationQualifier(RoleB.class), false, false));
        Comparator<BindRule> cmp = desire.ruleComparator();
        
        List<BindRule> brs = new ArrayList<BindRule>();
        brs.add(b3);
        brs.add(b1);
        brs.add(b2);
        
        Collections.sort(brs, cmp);
        Assert.assertEquals(b1, brs.get(0));
        Assert.assertEquals(b2, brs.get(1));
        Assert.assertEquals(b3, brs.get(2));
    }
    
    private ReflectionDesire getDefaultDesire(Object methodOrCtorParam, List<ReflectionDesire>  desires) {
        for (ReflectionDesire d: desires) {
            if (methodOrCtorParam instanceof Method) {
                if (d.getInjectionPoint() instanceof SetterInjectionPoint) {
                    SetterInjectionPoint sp = (SetterInjectionPoint) (d.getInjectionPoint());
                    if (sp.getSetterMethod().equals(methodOrCtorParam)) {
                        return (ReflectionDesire) d.getDefaultDesire();
                    }
                }
            } else { // assume its an Integer
                if (d.getInjectionPoint() instanceof ConstructorParameterInjectionPoint) {
                    ConstructorParameterInjectionPoint cp = (ConstructorParameterInjectionPoint) (d.getInjectionPoint());
                    if (((Integer) methodOrCtorParam).intValue() == cp.getParameterIndex()) {
                        return (ReflectionDesire) d.getDefaultDesire();
                    }
                }
            }
        }
        
        return null;
    }
    
    public static class A { }
    
    public static class B extends A { }
    
    public static class C { }
}
