package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleB;
import org.grouplens.inject.spi.reflect.types.RoleD;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionBindRuleTest {
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
        AnnotationRole br = (bindRole == null ? null : new AnnotationRole(bindRole));
        AnnotationRole dr = (desireRole == null ? null : new AnnotationRole(desireRole));
        ClassBindRule rule = new ClassBindRule(bindType, bindType, br, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(desireType, dr, false));
        
        Assert.assertEquals(expected, rule.matches(desire));
    }
    
    @Test
    public void testSatisfiableClassBindRuleSuccess() throws Exception {
        ClassBindRule rule = new ClassBindRule(B.class, A.class, null, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(A.class, null, false));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(B.class, ((ClassSatisfaction) applied.getSatisfaction()).getErasedType());
    }
    
    @Test
    public void testUnsatisfiableClassBindRuleSuccess() throws Exception {
        ClassBindRule rule = new ClassBindRule(C.class, C.class, null, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(C.class, null, false));
        
        ReflectionDesire applied = (ReflectionDesire) rule.apply(desire);
        Assert.assertNull(applied.getSatisfaction());
        Assert.assertEquals(C.class, applied.getDesiredType());
    }
    
    @Test
    public void testInstanceBindRuleSuccess() throws Exception {
        C instance = new D();
        InstanceBindRule rule = new InstanceBindRule(instance, C.class, null, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(C.class, null, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(InstanceSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(instance, ((InstanceSatisfaction) applied.getSatisfaction()).getInstance());
    }
    
    @Test
    public void testProviderClassBindRuleSuccess() throws Exception {
        ProviderClassBindRule rule = new ProviderClassBindRule(PA.class, A.class, null, false);
        ReflectionDesire desire = new ReflectionDesire(new MockInjectionPoint(A.class, null, false));
        
        Desire applied = rule.apply(desire);
        Assert.assertNotNull(applied.getSatisfaction());
        Assert.assertEquals(ProviderClassSatisfaction.class, applied.getSatisfaction().getClass());
        Assert.assertEquals(PA.class, ((ProviderClassSatisfaction) applied.getSatisfaction()).getProviderType());
    }
    
    @Test
    public void testProviderInstanceBindRuleSuccess() throws Exception {
        PA instance = new PA();
        ProviderInstanceBindRule rule = new ProviderInstanceBindRule(instance, A.class, null, false);
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
