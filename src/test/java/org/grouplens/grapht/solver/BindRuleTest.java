/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.reflect.internal.*;
import org.grouplens.grapht.reflect.internal.types.*;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class BindRuleTest {
    @Test
    public void testEquals() {
        // test various permutations of bind rule configurations
        TypeA instance = new TypeA();

        BindRule b1 = BindRuleBuilder.create()
                                     .setDependencyType(TypeA.class)
                                     .setImplementation(TypeA.class)
                                     .setCachePolicy(CachePolicy.NO_PREFERENCE)
                                     .setQualifierMatcher(Qualifiers.matchAny())
                                     .setTerminal(false)
                                     .build();
        BindRule b2 = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(new InstanceSatisfaction(instance)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        BindRule b3 = BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.match(RoleA.class)).setTerminal(false).build();

        Assert.assertEquals(b1, BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build());
        Assert.assertFalse(b1.equals(BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeB.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build()));
        Assert.assertFalse(b1.equals(BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(true).build()));
        Assert.assertFalse(b1.equals(BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NEW_INSTANCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build()));

        Assert.assertEquals(b2, BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(new InstanceSatisfaction(instance)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build());
        Assert.assertFalse(b2.equals(BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(new ProviderClassSatisfaction(ProviderA.class)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build()));

        Assert.assertEquals(b3, BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.match(RoleA.class)).setTerminal(false).build());
        Assert.assertFalse(b3.equals(BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.match(RoleD.class)).setTerminal(false).build()));
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
        QualifierMatcher br = (bindRole == null ? Qualifiers.matchAny() : Qualifiers.match(bindRole));
        Annotation dr = (desireRole == null ? null : new AnnotationBuilder(desireRole).build());
        BindRule rule = BindRuleBuilder.create().setDependencyType(bindType).setImplementation(bindType).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(br).setTerminal(false).build();
            
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
        Satisfaction satisfaction = (nullableSatisfaction ? new NullSatisfaction(TypeA.class) 
                                                          : new ClassSatisfaction(TypeA.class));

        BindRule rule = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(satisfaction).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(true).build();
        ReflectionDesire desire = new ReflectionDesire(injectPoint);
        
        Assert.assertEquals(expected, rule.matches(desire));
    }
    
    @Test
    public void testSatisfiableClassBindRuleSuccess() throws Exception {
        BindRule rule = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(Satisfactions.type(TypeB.class)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Assert.assertTrue(rule.matches(desire));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(TypeB.class, ((ClassSatisfaction) applied.getSatisfaction()).getErasedType());
    }
    
    @Test
    public void testUnsatisfiableClassBindRuleSuccess() throws Exception {
        BindRule rule = BindRuleBuilder.create().setDependencyType(InterfaceA.class).setImplementation(InterfaceA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(InterfaceA.class, false));
        
        Assert.assertTrue(rule.matches(desire));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNull(applied.getSatisfaction());
        Assert.assertEquals(InterfaceA.class, applied.getDesiredType());
    }
    
    @Test
    public void testInstanceBindRuleSuccess() throws Exception {
        TypeA instance = new TypeB();
        BindRule rule = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(Satisfactions.instance(instance)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Assert.assertTrue(rule.matches(desire));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(InstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((InstanceSatisfaction) applied.getSatisfaction()).getInstance());
    }
    
    @Test
    public void testNullInstanceBindRuleSuccess() throws Exception {
        BindRule rule = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(Satisfactions.nullOfType(TypeA.class)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, true));
        
        Assert.assertTrue(rule.matches(desire));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(NullSatisfaction.class, applied.getSatisfaction().getClass());
    }
    
    @Test
    public void testProviderClassBindRuleSuccess() throws Exception {
        BindRule rule = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(Satisfactions.providerType(ProviderA.class)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
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
        BindRule rule = BindRuleBuilder.create().setDependencyType(TypeA.class).setSatisfaction(Satisfactions.providerInstance(instance)).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(TypeA.class, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ProviderInstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((ProviderInstanceSatisfaction) applied.getSatisfaction()).getProvider());
    }

    @Test
    public void testCopyBuilder() {
        BindRule b1 = BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.NO_PREFERENCE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        BindRule b2 = BindRuleBuilder.create().setDependencyType(TypeA.class).setImplementation(TypeA.class).setCachePolicy(CachePolicy.MEMOIZE).setQualifierMatcher(Qualifiers.matchAny()).setTerminal(false).build();
        assertThat(b1.newCopyBuilder()
                     .build(),
                   equalTo(b1));
        assertThat(b1.newCopyBuilder()
                     .setCachePolicy(CachePolicy.MEMOIZE)
                     .build(),
                   equalTo(b2));
    }
}
