/*
 * LensKit, an open source recommender systems toolkit.
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

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.reflect.types.InterfaceA;
import org.grouplens.inject.spi.reflect.types.ProviderA;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleB;
import org.grouplens.inject.spi.reflect.types.RoleD;
import org.grouplens.inject.spi.reflect.types.TypeA;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionBindRuleTest {
    @Test
    public void testTerminatesChain() {
        Assert.assertTrue(new InstanceBindRule(new Object(), Object.class, null, 0).terminatesChain());
        Assert.assertTrue(new ProviderInstanceBindRule(new ProviderA(), InterfaceA.class, null, 0).terminatesChain());
        Assert.assertTrue(new ProviderClassBindRule(ProviderA.class, InterfaceA.class, null, 0).terminatesChain());
        
        Assert.assertFalse(new ClassBindRule(TypeA.class, InterfaceA.class, null, 0, false).terminatesChain());
        Assert.assertTrue(new ClassBindRule(TypeA.class, InterfaceA.class, null, 0, true).terminatesChain());
    }
    
    @Test
    public void testClassBindRuleEquals() throws Exception {
        // four variations of the arguments
        ClassBindRule rule1 = new ClassBindRule(A.class, A.class, null, 0, false);
        ClassBindRule rule2 = new ClassBindRule(B.class, A.class, null, 0, false);
        ClassBindRule rule3 = new ClassBindRule(A.class, A.class, new AnnotationQualifier(RoleA.class), 0, false);
        ClassBindRule rule4 = new ClassBindRule(A.class, A.class, null, 1, false);
        
        Assert.assertEquals(rule1, new ClassBindRule(A.class, A.class, null, 0, false));
        Assert.assertEquals(rule2, new ClassBindRule(B.class, A.class, null, 0, false));
        Assert.assertEquals(rule3, new ClassBindRule(A.class, A.class, new AnnotationQualifier(RoleA.class), 0, false));
        Assert.assertEquals(rule4, new ClassBindRule(A.class, A.class, null, 1, false));
        
        Assert.assertFalse(rule1.equals(rule2));
        Assert.assertFalse(rule2.equals(rule3));
        Assert.assertFalse(rule3.equals(rule4));
        Assert.assertFalse(rule1.equals(rule4));
    }
    
    @Test
    public void testInstanceBindRuleEquals() throws Exception {
        // four variations of the arguments
        A i1 = new A();
        A i2 = new A();
        
        InstanceBindRule rule1 = new InstanceBindRule(i1, A.class, null, 0);
        InstanceBindRule rule2 = new InstanceBindRule(i1, A.class, null, 1);
        InstanceBindRule rule3 = new InstanceBindRule(i2, A.class, new AnnotationQualifier(RoleA.class), 0);
        InstanceBindRule rule4 = new InstanceBindRule(i2, A.class, null, 1);
        
        Assert.assertEquals(rule1, new InstanceBindRule(i1, A.class, null, 0));
        Assert.assertEquals(rule2, new InstanceBindRule(i1, A.class, null, 1));
        Assert.assertEquals(rule3, new InstanceBindRule(i2, A.class, new AnnotationQualifier(RoleA.class), 0));
        Assert.assertEquals(rule4, new InstanceBindRule(i2, A.class, null, 1));
        
        Assert.assertFalse(rule1.equals(new InstanceBindRule(i2, A.class, null, 0)));
        
        Assert.assertFalse(rule1.equals(rule2));
        Assert.assertFalse(rule2.equals(rule3));
        Assert.assertFalse(rule3.equals(rule4));
        Assert.assertFalse(rule1.equals(rule4));
    }
    
    @Test
    public void testProviderBindRuleEquals() throws Exception {
        // four variations of the arguments
        ProviderClassBindRule rule1 = new ProviderClassBindRule(PA.class, A.class, null, 0);
        ProviderClassBindRule rule2 = new ProviderClassBindRule(PA.class, A.class, null, 1);
        ProviderClassBindRule rule3 = new ProviderClassBindRule(PA.class, A.class, new AnnotationQualifier(RoleA.class), 0);
        
        Assert.assertEquals(rule1, new ProviderClassBindRule(PA.class, A.class, null, 0));
        Assert.assertEquals(rule2, new ProviderClassBindRule(PA.class, A.class, null, 1));
        Assert.assertEquals(rule3, new ProviderClassBindRule(PA.class, A.class, new AnnotationQualifier(RoleA.class), 0));
        
        Assert.assertFalse(rule1.equals(rule2));
        Assert.assertFalse(rule2.equals(rule3));
        Assert.assertFalse(rule1.equals(rule3));
    }
    
    @Test
    public void testProviderInstanceBindRuleEquals() throws Exception {
        // four variations of the arguments
        PA p1 = new PA();
        PA p2 = new PA();
        
        ProviderInstanceBindRule rule1 = new ProviderInstanceBindRule(p1, A.class, null, 0);
        ProviderInstanceBindRule rule2 = new ProviderInstanceBindRule(p1, A.class, null, 1);
        ProviderInstanceBindRule rule3 = new ProviderInstanceBindRule(p2, A.class, new AnnotationQualifier(RoleA.class), 0);
        
        Assert.assertEquals(rule1, new ProviderInstanceBindRule(p1, A.class, null, 0));
        Assert.assertEquals(rule2, new ProviderInstanceBindRule(p1, A.class, null, 1));
        Assert.assertEquals(rule3, new ProviderInstanceBindRule(p2, A.class, new AnnotationQualifier(RoleA.class), 0));
        
        Assert.assertFalse(rule1.equals(rule2));
        Assert.assertFalse(rule2.equals(rule3));
        Assert.assertFalse(rule1.equals(rule3));
    }
    
    @Test
    public void testPrimitiveMatch() throws Exception {
        // test boxing/unboxing of types
        doMatchTest(Integer.class, null, int.class, null, true);
        doMatchTest(int.class, null, Integer.class, null, true);
    }
    
    @Test
    public void testExactClassNoRoleMatch() throws Exception {
        doMatchTest(A.class, null, A.class, null, true);
    }
    
    @Test
    public void testExactClassExactRoleMatch() throws Exception {
        doMatchTest(A.class, RoleA.class, A.class, RoleA.class, true);
    }
    
    @Test
    public void testExactClassSubRoleMatch() throws Exception {
        doMatchTest(A.class, RoleB.class, A.class, RoleA.class, true);
    }
    
    @Test
    public void testSubclassNoMatch() throws Exception {
        doMatchTest(B.class, null, A.class, null, false);
    }
    
    @Test
    public void testNoInheritenceNoMatch() throws Exception {
        doMatchTest(C.class, null, A.class, null, false);
        doMatchTest(A.class, null, B.class, null, false);
    }
    
    @Test
    public void testNoRoleInheritenceNoMatch() throws Exception {
        doMatchTest(A.class, RoleA.class, A.class, RoleD.class, false);
    }
    
    @Test
    public void testSuperRoleNoMatch() throws Exception {
        doMatchTest(A.class, RoleA.class, A.class, RoleB.class, false);
    }
    
    private void doMatchTest(Class<?> desireType, Class<? extends Annotation> desireRole,
                             Class<?> bindType, Class<? extends Annotation> bindRole,
                             boolean expected) throws Exception {
        AnnotationQualifier br = (bindRole == null ? null : new AnnotationQualifier(bindRole));
        AnnotationQualifier dr = (desireRole == null ? null : new AnnotationQualifier(desireRole));
        ClassBindRule rule = new ClassBindRule(bindType, bindType, br, 0, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(desireType, dr, false));
        
        Assert.assertEquals(expected, rule.matches(desire));
    }
    
    @Test
    public void testSatisfiableClassBindRuleSuccess() throws Exception {
        ClassBindRule rule = new ClassBindRule(B.class, A.class, null, 0, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(A.class, null, false));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(B.class, ((ClassSatisfaction) applied.getSatisfaction()).getErasedType());
    }
    
    @Test
    public void testUnsatisfiableClassBindRuleSuccess() throws Exception {
        ClassBindRule rule = new ClassBindRule(C.class, C.class, null, 0, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(C.class, null, false));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNull(applied.getSatisfaction());
        Assert.assertEquals(C.class, applied.getDesiredType());
    }
    
    @Test
    public void testInstanceBindRuleSuccess() throws Exception {
        C instance = new D();
        InstanceBindRule rule = new InstanceBindRule(instance, C.class, null, 0);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(C.class, null, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(InstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((InstanceSatisfaction) applied.getSatisfaction()).getInstance());
    }
    
    @Test
    public void testProviderClassBindRuleSuccess() throws Exception {
        ProviderClassBindRule rule = new ProviderClassBindRule(PA.class, A.class, null, 0);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(A.class, null, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ProviderClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(PA.class, ((ProviderClassSatisfaction) applied.getSatisfaction()).getProviderType());
    }
    
    @Test
    public void testProviderInstanceBindRuleSuccess() throws Exception {
        PA instance = new PA();
        ProviderInstanceBindRule rule = new ProviderInstanceBindRule(instance, A.class, null, 0);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(A.class, null, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ProviderInstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((ProviderInstanceSatisfaction) applied.getSatisfaction()).getProvider());
    }
    
    public static class A { }
    
    public static class B extends A { }
    
    public static abstract class C { }
    
    public static class D extends C { }
    
    public static class PA implements Provider<A> {
        @Override
        public A get() {
            return new B();
        }
    }
}
