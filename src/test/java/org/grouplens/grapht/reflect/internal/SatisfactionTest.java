/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.reflect.internal;

import com.google.common.collect.Maps;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.Instantiators;
import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.reflect.Satisfaction;
import org.grouplens.grapht.reflect.internal.types.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SatisfactionTest {
    private InjectionPoint ctorProviderCIP;
    
    private Set<InjectionPoint> typeCInjectPoints;
    private Set<InjectionPoint> providerCInjectPoints;
    
    @Before
    public void setup() throws Exception {
        ctorProviderCIP = new ParameterInjectionPoint(ProviderC.class.getConstructor(int.class), 0);
        
        typeCInjectPoints = new HashSet<InjectionPoint>();
        providerCInjectPoints = new HashSet<InjectionPoint>();
        
        typeCInjectPoints.add(TypeC.CONSTRUCTOR);
        typeCInjectPoints.add(TypeC.INTERFACE_A);
        typeCInjectPoints.add(TypeC.TYPE_A);
        typeCInjectPoints.add(TypeC.INTERFACE_B);
        typeCInjectPoints.add(TypeC.TYPE_B);
        
        providerCInjectPoints.add(ctorProviderCIP);
    }
    
    private Set<InjectionPoint> getInjectionPoints(Satisfaction s) {
        Set<InjectionPoint> detected = new HashSet<InjectionPoint>();
        for (Desire d: s.getDependencies()) {
            detected.add(d.getInjectionPoint());
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

        Map<Desire,Instantiator> providers = Maps.newHashMap();
        providers.put(new ReflectionDesire(TypeC.CONSTRUCTOR), Instantiators.ofInstance(10));
        providers.put(new ReflectionDesire(TypeC.INTERFACE_A), Instantiators.ofInstance(a1));
        providers.put(new ReflectionDesire(TypeC.TYPE_A), Instantiators.ofInstance(a2));
        providers.put(new ReflectionDesire(TypeC.INTERFACE_B), Instantiators.ofInstance(b1));
        providers.put(new ReflectionDesire(TypeC.TYPE_B), Instantiators.ofInstance(b2));
        
        Instantiator provider = new ClassSatisfaction(TypeC.class).makeInstantiator(providers, null);
        Object o = provider.instantiate();
        
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

        Instantiator p = new InstanceSatisfaction(c).makeInstantiator(Collections.EMPTY_MAP,
                                                                      null);
        Assert.assertSame(c, p.instantiate());
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
        Map<Desire,Instantiator> providers = Maps.newHashMap();
        providers.put(new ReflectionDesire(ctorProviderCIP), Instantiators.ofInstance(10));
        Instantiator provider = new ProviderClassSatisfaction(ProviderC.class).makeInstantiator(providers, null);
        // Assert.assertTrue(provider instanceof ProviderC);
        
        TypeC c = (TypeC) provider.instantiate();
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
    public void testClassSatisfactionEquals() throws Exception {
        // two variations of the arguments
        ClassSatisfaction s1 = new ClassSatisfaction(A.class);
        ClassSatisfaction s2 = new ClassSatisfaction(B.class);
        
        Assert.assertEquals(s1, new ClassSatisfaction(A.class));
        Assert.assertEquals(s2, new ClassSatisfaction(B.class));
        Assert.assertEquals(s1, s1);
        Assert.assertEquals(s2, s2);
        
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s2.equals(s1));
        Assert.assertFalse(s1.equals(new Object()));
        
    }
    
    @Test
    public void testNullSatisfactionEquals() throws Exception {
        // two variations of the arguments
        NullSatisfaction s1 = new NullSatisfaction(A.class);
        NullSatisfaction s2 = new NullSatisfaction(B.class);
        
        Assert.assertEquals(s1, new NullSatisfaction(A.class));
        Assert.assertEquals(s2, new NullSatisfaction(B.class));
        Assert.assertEquals(s1, s1);
        Assert.assertEquals(s2, s2);
        
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s2.equals(s1));
        Assert.assertFalse(s1.equals(new Object()));
    }
    
    @Test
    public void testInstanceSEquals() throws Exception {
        // three variations of the arguments
        A a1 = new A();
        A a2 = new A();
        B b1 = new B();
        
        InstanceSatisfaction s1 = new InstanceSatisfaction(a1);
        InstanceSatisfaction s2 = new InstanceSatisfaction(a2);
        InstanceSatisfaction s3 = new InstanceSatisfaction(b1);
        
        Assert.assertEquals(s1, new InstanceSatisfaction(a1));
        Assert.assertEquals(s2, new InstanceSatisfaction(a2));
        Assert.assertEquals(s3, new InstanceSatisfaction(b1));
        Assert.assertEquals(s1, s1);
        Assert.assertEquals(s2, s2);
        Assert.assertEquals(s3, s3);
        
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s2.equals(s3));
        Assert.assertFalse(s3.equals(s1));
        Assert.assertFalse(s1.equals(new Object()));
    }
    
    @Test
    public void testProviderClassSatisfactionEquals() throws Exception {
        // two variations of the arguments
        ProviderClassSatisfaction s1 = new ProviderClassSatisfaction(PA.class);
        ProviderClassSatisfaction s2 = new ProviderClassSatisfaction(PB.class);
        
        Assert.assertEquals(s1, new ProviderClassSatisfaction(PA.class));
        Assert.assertEquals(s2, new ProviderClassSatisfaction(PB.class));
        Assert.assertEquals(s1, s1);
        Assert.assertEquals(s2, s2);
        
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s2.equals(s1));
        Assert.assertFalse(s1.equals(new Object()));
    }
    
    @Test
    public void testProviderInstanceSatisfactionEquals() throws Exception {
     // three variations of the arguments
        PA a1 = new PA();
        PA a2 = new PA();
        PB b1 = new PB();
        
        ProviderInstanceSatisfaction s1 = new ProviderInstanceSatisfaction(a1);
        ProviderInstanceSatisfaction s2 = new ProviderInstanceSatisfaction(a2);
        ProviderInstanceSatisfaction s3 = new ProviderInstanceSatisfaction(b1);
        
        Assert.assertEquals(s1, new ProviderInstanceSatisfaction(a1));
        Assert.assertEquals(s2, new ProviderInstanceSatisfaction(a2));
        Assert.assertEquals(s3, new ProviderInstanceSatisfaction(b1));
        Assert.assertEquals(s1, s1);
        Assert.assertEquals(s2, s2);
        Assert.assertEquals(s3, s3);
        
        Assert.assertFalse(s1.equals(s2));
        Assert.assertFalse(s2.equals(s3));
        Assert.assertFalse(s3.equals(s1));
        Assert.assertFalse(s1.equals(new Object()));
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
    
    public static class PB implements Provider<B> {
        @Override
        public @Nullable B get() {
            return null;
        }
    }
}
