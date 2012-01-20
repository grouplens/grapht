/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Provider;

import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.reflect.types.InterfaceA;
import org.grouplens.inject.spi.reflect.types.InterfaceB;
import org.grouplens.inject.spi.reflect.types.ProviderC;
import org.grouplens.inject.spi.reflect.types.RoleD;
import org.grouplens.inject.spi.reflect.types.TypeA;
import org.grouplens.inject.spi.reflect.types.TypeB;
import org.grouplens.inject.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReflectionSatisfactionTest {
    private InjectionPoint ctorTypeCIP;
    private InjectionPoint roleAIP;
    private InjectionPoint typeAIP;
    private InjectionPoint roleEIP;
    private InjectionPoint typeBIP;
    
    private InjectionPoint ctorProviderCIP;
    
    private Set<InjectionPoint> typeCInjectPoints;
    private Set<InjectionPoint> providerCInjectPoints;
    
    @Before
    public void setup() throws Exception {
        ctorTypeCIP = new ConstructorParameterInjectionPoint(TypeC.class.getConstructor(int.class), 0);
        roleAIP = new SetterInjectionPoint(TypeC.class.getMethod("setRoleA", InterfaceA.class));
        typeAIP = new SetterInjectionPoint(TypeC.class.getMethod("setTypeA", TypeA.class));
        roleEIP = new SetterInjectionPoint(TypeC.class.getMethod("setRoleE", InterfaceB.class));
        typeBIP = new SetterInjectionPoint(TypeC.class.getMethod("setTypeB", TypeB.class));
        
        ctorProviderCIP = new ConstructorParameterInjectionPoint(ProviderC.class.getConstructor(int.class), 0);
        
        
        typeCInjectPoints = new HashSet<InjectionPoint>();
        providerCInjectPoints = new HashSet<InjectionPoint>();
        
        typeCInjectPoints.add(ctorTypeCIP);
        typeCInjectPoints.add(roleAIP);
        typeCInjectPoints.add(typeAIP);
        typeCInjectPoints.add(roleEIP);
        typeCInjectPoints.add(typeBIP);
        
        providerCInjectPoints.add(ctorProviderCIP);
    }
    
    private Set<InjectionPoint> getInjectionPoints(ReflectionSatisfaction s) {
        Set<InjectionPoint> detected = new HashSet<InjectionPoint>();
        for (Desire d: s.getDependencies()) {
            detected.add(((ReflectionDesire) d).getInjectionPoint());
        }
        return detected;
    }
    
    @Test
    public void testClassSatisfactionDesires() throws Exception {
        ClassSatisfaction s = new ClassSatisfaction(TypeC.class);
        Set<InjectionPoint> d = getInjectionPoints(s);
        Assert.assertEquals(typeCInjectPoints, d);
        Assert.assertEquals(TypeC.class, s.getErasedType());
    }
    
    @Test
    public void testClassSatisfactionProvider() throws Exception {
        InterfaceA a1 = new TypeA();
        TypeA a2 = new TypeA();
        InterfaceB b1 = new TypeB();
        TypeB b2 = new TypeB();
        
        MockProviderFunction providers = new MockProviderFunction();
        providers.add(ctorTypeCIP, new InstanceProvider<Integer>(10));
        providers.add(roleAIP, new InstanceProvider<InterfaceA>(a1));
        providers.add(typeAIP, new InstanceProvider<TypeA>(a2));
        providers.add(roleEIP, new InstanceProvider<InterfaceB>(b1));
        providers.add(typeBIP, new InstanceProvider<TypeB>(b2));
        
        Provider<?> provider = new ClassSatisfaction(TypeC.class).makeProvider(providers);
        Object o = provider.get();
        
        Assert.assertTrue(o instanceof TypeC);
        
        TypeC c = (TypeC) o;
        Assert.assertSame(a1, c.getInterfaceA());
        Assert.assertSame(a2, c.getTypeA());
        Assert.assertSame(b1, c.getInterfaceB());
        Assert.assertSame(b2, c.getTypeB());
        Assert.assertEquals(10, c.getIntValue());
    }
    
    @Test
    public void testInstanceSatisfactionDesires() throws Exception {
        TypeC c = new TypeC(4);
        InstanceSatisfaction s = new InstanceSatisfaction(c);
        Set<InjectionPoint> d = getInjectionPoints(s);
        Assert.assertTrue(d.isEmpty());
        Assert.assertSame(c, s.getInstance());
    }
    
    @Test
    public void testInstanceSatisfactionProvider() throws Exception {
        TypeC c = new TypeC(4);
        Provider<?> p = new InstanceSatisfaction(c).makeProvider(new MockProviderFunction());
        Assert.assertSame(c, p.get());
    }
    
    @Test
    public void testProviderClassSatisfactionDesires() throws Exception {
        ProviderClassSatisfaction s = new ProviderClassSatisfaction(ProviderC.class);
        Set<InjectionPoint> d = getInjectionPoints(s);
        Assert.assertEquals(providerCInjectPoints, d);
        Assert.assertEquals(ProviderC.class, s.getProviderType());
    }
    
    @Test
    public void testProviderClassSatisfactionProvider() throws Exception {
        MockProviderFunction providers = new MockProviderFunction();
        providers.add(ctorProviderCIP, new InstanceProvider<Integer>(10));
        
        Provider<?> provider = new ProviderClassSatisfaction(ProviderC.class).makeProvider(providers);
        Assert.assertTrue(provider instanceof ProviderC);
        
        TypeC c = (TypeC) provider.get();
        Assert.assertEquals(10, c.getIntValue());
        Assert.assertNull(c.getInterfaceA());
        Assert.assertNull(c.getTypeA());
        Assert.assertNull(c.getInterfaceB());
        Assert.assertNull(c.getTypeB());
    }
    
    @Test
    public void testProviderInstanceSatisfactionDesires() throws Exception {
        ProviderC c = new ProviderC(4);
        ProviderInstanceSatisfaction s = new ProviderInstanceSatisfaction(c);
        Set<InjectionPoint> d = getInjectionPoints(s);
        Assert.assertTrue(d.isEmpty());
        Assert.assertSame(c, s.getProvider());
    }
    
    @Test
    public void testProviderInstanceSatisfactionProvider() throws Exception {
        ProviderC instance = new ProviderC(10);
        Provider<?> provider = new ProviderInstanceSatisfaction(instance).makeProvider(new MockProviderFunction());
        Assert.assertSame(instance, provider);
    }
    
    @Test
    public void testContextMatcherComparator() throws Exception {
        ContextMatcher cm1 = new ReflectionContextMatcher(TypeB.class, null); // type dist = 0, annot dist = 0
        ContextMatcher cm2 = new ReflectionContextMatcher(TypeA.class, new AnnotationRole(RoleD.class)); // type dist = 1, annot dist = 0
        ContextMatcher cm3 = new ReflectionContextMatcher(TypeA.class, null); // type dist = 1, annot dist = 1
        
        List<ContextMatcher> cms = new ArrayList<ContextMatcher>();
        cms.add(cm3);
        cms.add(cm1);
        cms.add(cm2);
        
        Collections.sort(cms, new ClassSatisfaction(TypeB.class).contextComparator(new AnnotationRole(RoleD.class)));
        Assert.assertEquals(cm1, cms.get(0));
        Assert.assertEquals(cm2, cms.get(1));
        Assert.assertEquals(cm3, cms.get(2));
    }
    
    private static class InstanceProvider<T> implements Provider<T> {
        private T instance;
        
        public InstanceProvider(T t) {
            instance = t;
        }
        
        @Override
        public T get() {
            return instance;
        }
    }
}
