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
package org.grouplens.grapht.solver;

import java.lang.annotation.Annotation;

import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.spi.MockInjectionPoint;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.reflect.ClassSatisfaction;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.NullSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderClassSatisfaction;
import org.grouplens.grapht.spi.reflect.ProviderInstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.ReflectionDesire;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionSatisfaction;
import org.grouplens.grapht.spi.reflect.types.InterfaceA;
import org.grouplens.grapht.spi.reflect.types.ProviderA;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.spi.reflect.types.RoleB;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.spi.reflect.types.TypeA;
import org.grouplens.grapht.spi.reflect.types.TypeB;
import org.grouplens.grapht.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReflectionBindRuleTest {
    private ReflectionInjectSPI spi;
    
    @Before
    public void setup() {
        spi = new ReflectionInjectSPI();
    }
    
    @Test
    public void testEquals() {
        // test various permutations of bind rule configurations
        TypeA instance = new TypeA();
        
        BindRule b1 = new BindRule(TypeA.class, TypeA.class, CachePolicy.NO_PREFERENCE, spi.matchAny(), false);
        BindRule b2 = new BindRule(TypeA.class, new InstanceSatisfaction(instance), CachePolicy.NO_PREFERENCE, spi.matchAny(), false);
        BindRule b3 = new BindRule(TypeA.class, TypeA.class, CachePolicy.NO_PREFERENCE, spi.match(RoleA.class), false);
        
        Assert.assertEquals(b1, new BindRule(TypeA.class, TypeA.class, CachePolicy.NO_PREFERENCE, spi.matchAny(), false));
        Assert.assertFalse(b1.equals(new BindRule(TypeA.class, TypeB.class, CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Assert.assertFalse(b1.equals(new BindRule(TypeA.class, TypeA.class, CachePolicy.NO_PREFERENCE, spi.matchAny(), true)));
        Assert.assertFalse(b1.equals(new BindRule(TypeA.class, TypeA.class, CachePolicy.NEW_INSTANCE, spi.matchAny(), false)));

        Assert.assertEquals(b2, new BindRule(TypeA.class, new InstanceSatisfaction(instance), CachePolicy.NO_PREFERENCE, spi.matchAny(), false));
        Assert.assertFalse(b2.equals(new BindRule(TypeA.class, new ProviderClassSatisfaction(ProviderA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        
        Assert.assertEquals(b3, new BindRule(TypeA.class, TypeA.class, CachePolicy.NO_PREFERENCE, spi.match(RoleA.class), false));
        Assert.assertFalse(b3.equals(new BindRule(TypeA.class, TypeA.class, CachePolicy.NO_PREFERENCE, spi.match(RoleD.class), false)));
    }
    
    @Test
    public void testPrimitiveMatch() throws Exception {
        // test boxing/unboxing of types
        doMatchTest(Integer.class, null, int.class, null, true);
        doMatchTest(int.class, null, Integer.class, null, true);
    }
    
    @Test
    public void testExactClassNoRoleMatch() throws Exception {
        doMatchTest(TypeA.class, null, TypeA.class, null, true);
    }
    
    @Test
    public void testExactClassExactRoleMatch() throws Exception {
        doMatchTest(TypeA.class, RoleA.class, TypeA.class, RoleA.class, true);
    }
    
    @Test
    public void testSubclassNoMatch() throws Exception {
        doMatchTest(TypeB.class, null, TypeA.class, null, false);
    }
    
    @Test
    public void testNoInheritenceNoMatch() throws Exception {
        doMatchTest(TypeC.class, null, TypeA.class, null, false);
        doMatchTest(TypeA.class, null, TypeB.class, null, false);
    }
    
    @Test
    public void testNoRoleInheritenceNoMatch() throws Exception {
        doMatchTest(TypeA.class, RoleA.class, TypeA.class, RoleD.class, false);
    }
    
    @Test
    public void testSuperRoleNoMatch() throws Exception {
        doMatchTest(TypeA.class, RoleA.class, TypeA.class, RoleB.class, false);
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void doMatchTest(Class desireType, Class<? extends Annotation> desireRole,
                             Class bindType, Class<? extends Annotation> bindRole,
                             boolean expected) throws Exception {
        QualifierMatcher br = (bindRole == null ? spi.matchAny() : spi.match(bindRole));
        Annotation dr = (desireRole == null ? null : new AnnotationBuilder(desireRole).build());
        BindRule rule = new BindRule(bindType, bindType, CachePolicy.NO_PREFERENCE, br, false);
            
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(desireType, dr, false));
        
        Assert.assertEquals(expected, rule.matches(desire));
    }
    
    @Test
    public void testNullable() throws Exception {
        doNullableTest(false, false, true);
        doNullableTest(true, false, true);
        doNullableTest(false, true, true);
        doNullableTest(true, true, true);
    }
    
    private void doNullableTest(boolean nullableDesire, boolean nullableSatisfaction, boolean expected) throws Exception {
        InjectionPoint injectPoint = new MockInjectionPoint(TypeA.class, nullableDesire);
        ReflectionSatisfaction satisfaction = (nullableSatisfaction ? new NullSatisfaction(TypeA.class) 
                                                                    : new ClassSatisfaction(TypeA.class));
        
        BindRule rule = new BindRule(TypeA.class, satisfaction, CachePolicy.NO_PREFERENCE, spi.matchAny(), true);
        ReflectionDesire desire = new ReflectionDesire(injectPoint);
        
        Assert.assertEquals(expected, rule.matches(desire));
    }
    
    @Test
    public void testSatisfiableClassBindRuleSuccess() throws Exception {
        BindRule rule = new BindRule(TypeA.class, spi.satisfy(TypeB.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false); 
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Assert.assertTrue(rule.matches(desire));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(TypeB.class, ((ClassSatisfaction) applied.getSatisfaction()).getErasedType());
    }
    
    @Test
    public void testUnsatisfiableClassBindRuleSuccess() throws Exception {
        BindRule rule = new BindRule(InterfaceA.class, InterfaceA.class, CachePolicy.NO_PREFERENCE, spi.matchAny(), false); 
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(InterfaceA.class, false));
        
        Assert.assertTrue(rule.matches(desire));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNull(applied.getSatisfaction());
        Assert.assertEquals(InterfaceA.class, applied.getDesiredType());
    }
    
    @Test
    public void testInstanceBindRuleSuccess() throws Exception {
        TypeA instance = new TypeB();
        BindRule rule = new BindRule(TypeA.class, spi.satisfy(instance), CachePolicy.NO_PREFERENCE, spi.matchAny(), false); 
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Assert.assertTrue(rule.matches(desire));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(InstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((InstanceSatisfaction) applied.getSatisfaction()).getInstance());
    }
    
    @Test
    public void testNullInstanceBindRuleSuccess() throws Exception {
        BindRule rule = new BindRule(TypeA.class, spi.satisfyWithNull(TypeA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false); 
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, true));
        
        Assert.assertTrue(rule.matches(desire));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(NullSatisfaction.class, applied.getSatisfaction().getClass());
    }
    
    @Test
    public void testProviderClassBindRuleSuccess() throws Exception {
        BindRule rule = new BindRule(TypeA.class, spi.satisfyWithProvider(ProviderA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false); 
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Assert.assertTrue(rule.matches(desire));

        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ProviderClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(ProviderA.class, ((ProviderClassSatisfaction) applied.getSatisfaction()).getProviderType());
    }
    
    @Test
    public void testProviderInstanceBindRuleSuccess() throws Exception {
        ProviderA instance = new ProviderA();
        BindRule rule = new BindRule(TypeA.class, spi.satisfyWithProvider(instance), CachePolicy.NO_PREFERENCE, spi.matchAny(), false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ProviderInstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((ProviderInstanceSatisfaction) applied.getSatisfaction()).getProvider());
    }
}
