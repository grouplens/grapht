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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Provider;

import org.apache.commons.lang3.tuple.Pair;
import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.Injector;
import org.grouplens.grapht.InjectorBuilder;
import org.grouplens.grapht.graph.Edge;
import org.grouplens.grapht.graph.Graph;
import org.grouplens.grapht.graph.Node;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DefaultInjector;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.InjectionPoint;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.types.CycleA;
import org.grouplens.grapht.spi.reflect.types.CycleB;
import org.grouplens.grapht.spi.reflect.types.InterfaceA;
import org.grouplens.grapht.spi.reflect.types.InterfaceB;
import org.grouplens.grapht.spi.reflect.types.ParameterA;
import org.grouplens.grapht.spi.reflect.types.ProviderA;
import org.grouplens.grapht.spi.reflect.types.RoleA;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.spi.reflect.types.TypeA;
import org.grouplens.grapht.spi.reflect.types.TypeB;
import org.grouplens.grapht.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Test;

public class ReflectionInjectionTest {
    @Test
    public void testProviderCycleInjection() throws Exception {
        InjectorBuilder b = new InjectorBuilder().setProviderInjectionEnabled(true);
        Injector i = b.build();
        
        i.getInstance(CycleA.class);
        Graph<Pair<Satisfaction, CachePolicy>, Desire> g = ((DefaultInjector) i).getSolver().getGraph();
        
        Assert.assertEquals(3 + 1, g.getNodes().size());
        Node<Pair<Satisfaction, CachePolicy>> root = g.getNode(null);
        
        Assert.assertEquals(1, g.getOutgoingEdges(root).size());
        Node<Pair<Satisfaction, CachePolicy>> anode = g.getOutgoingEdges(root).iterator().next().getTail();
        Assert.assertEquals(CycleA.class, anode.getLabel().getKey().getErasedType());
        
        Assert.assertEquals(1, g.getOutgoingEdges(anode).size());
        Node<Pair<Satisfaction, CachePolicy>> bnode = g.getOutgoingEdges(anode).iterator().next().getTail();
        Assert.assertEquals(CycleB.class, bnode.getLabel().getKey().getErasedType());
        
        Assert.assertEquals(1, g.getOutgoingEdges(bnode).size());
        Node<Pair<Satisfaction, CachePolicy>> pnode = g.getOutgoingEdges(bnode).iterator().next().getTail();
        Assert.assertEquals(Provider.class, pnode.getLabel().getKey().getErasedType());
        
        Assert.assertEquals(1, g.getOutgoingEdges(pnode).size());
        Node<Pair<Satisfaction, CachePolicy>> anode2 = g.getOutgoingEdges(pnode).iterator().next().getTail();
        Assert.assertSame(anode, anode2);
    }
    
    @Test
    public void testTypeCInjectionWithDefaults() throws Exception {
        // Test that TypeC can be resolved successfully without any bind rules.
        // All of TypeC's dependencies have defaults or are satisfiable.
        InjectSPI spi = new ReflectionInjectSPI();
        Desire rootDesire = spi.desire(null, TypeC.class, false);
        DefaultInjector r = new DefaultInjector(spi, new DefaultDesireBindingFunction(spi));
        
        TypeC instance = r.getInstance(TypeC.class);
        Assert.assertEquals(5, instance.getIntValue());
        Assert.assertNotNull(instance.getInterfaceA());
        Assert.assertTrue(instance.getInterfaceA() instanceof TypeB); // ProviderA actually creates TypeB's
        Assert.assertSame(instance.getInterfaceA(), instance.getTypeA());
        Assert.assertNotNull(instance.getInterfaceB());
        Assert.assertTrue(instance.getInterfaceB() instanceof TypeB);
        Assert.assertSame(instance.getInterfaceB(), instance.getTypeB());
        
        // also verify memoization
        Assert.assertSame(instance, r.getInstance(TypeC.class));
        
        Node<Pair<Satisfaction, CachePolicy>> resolvedRoot = r.getSolver().getGraph().getOutgoingEdge(r.getSolver().getRootNode(), rootDesire).getTail();
        Assert.assertEquals(5, r.getSolver().getGraph().getOutgoingEdges(resolvedRoot).size());
        
        Map<InjectionPoint, Node<Pair<Satisfaction, CachePolicy>>> deps = new HashMap<InjectionPoint, Node<Pair<Satisfaction, CachePolicy>>>();
        for (Edge<Pair<Satisfaction, CachePolicy>, Desire> e: r.getSolver().getGraph().getOutgoingEdges(resolvedRoot)) {
            ReflectionDesire d = (ReflectionDesire) e.getLabel();
            
            if (d.getInjectionPoint().equals(TypeC.CONSTRUCTOR)) {
                // CycleA ParameterA defaults to 5
                Assert.assertFalse(deps.containsKey(TypeC.CONSTRUCTOR));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof InstanceSatisfaction);
                Assert.assertEquals(5, ((InstanceSatisfaction) e.getTail().getLabel().getKey()).getInstance());
                deps.put(TypeC.CONSTRUCTOR, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_A)) {
                // An InterfaceA is implemented by TypeA, which is then provided by Provider CycleA
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_A));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof ProviderClassSatisfaction);
                Assert.assertEquals(ProviderA.class, ((ProviderClassSatisfaction) e.getTail().getLabel().getKey()).getProviderType());
                deps.put(TypeC.INTERFACE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_A)) {
                // CycleA TypeA is provided by a ProviderA
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_A));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof ProviderClassSatisfaction);
                Assert.assertEquals(ProviderA.class, ((ProviderClassSatisfaction) e.getTail().getLabel().getKey()).getProviderType());
                deps.put(TypeC.TYPE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_B)) {
                // RoleE inherits RoleD and that defaults to TypeB
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_B));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof ClassSatisfaction);
                Assert.assertEquals(TypeB.class, e.getTail().getLabel().getKey().getErasedType());
                deps.put(TypeC.INTERFACE_B, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_B)) {
                // TypeB is satisfiable on its own
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_B));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof ClassSatisfaction);
                Assert.assertEquals(TypeB.class, e.getTail().getLabel().getKey().getErasedType());
                deps.put(TypeC.TYPE_B, e.getTail());
            } else {
                Assert.fail();
            }
        }
        
        // verify that all injection points were tested
        Assert.assertTrue(deps.containsKey(TypeC.CONSTRUCTOR));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_A));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_A));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_B));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_B));
        
        // make sure that nodes are shared where appropriate
        Assert.assertSame(deps.get(TypeC.INTERFACE_A), deps.get(TypeC.TYPE_A));
        Assert.assertSame(deps.get(TypeC.INTERFACE_B), deps.get(TypeC.TYPE_B));
    }
    
    @Test
    public void testTypeCInjectionWithBindings() throws Exception {
        // Test that TypeC can be injected correctly using bind rules, although
        // the bind rule configuration does not need to be very complicated, since
        // the resolver and bind rules are already tested.
        InjectSPI spi = new ReflectionInjectSPI();
        Desire rootDesire = spi.desire(null, TypeC.class, false);
        
        TypeA a = new TypeA();
        TypeB b = new TypeB();
        
        BindingFunctionBuilder bindRules = new BindingFunctionBuilder(spi, false);
        bindRules.getRootContext().bind(ParameterA.class, 10);
        bindRules.getRootContext().bind(InterfaceA.class).withQualifier(RoleA.class).to(PrimeA.class);
        bindRules.getRootContext().bind(InterfaceB.class).withQualifier(RoleD.class).to(PrimeB.class);
        bindRules.getRootContext().bind(TypeA.class).to(a);
        bindRules.getRootContext().bind(TypeB.class).to(b);
        
        DefaultInjector r = new DefaultInjector(spi, bindRules.getFunction(RuleSet.EXPLICIT), new DefaultDesireBindingFunction(spi));

        TypeC instance = r.getInstance(TypeC.class);
        Assert.assertEquals(10, instance.getIntValue());
        Assert.assertNotNull(instance.getInterfaceA());
        Assert.assertTrue(instance.getInterfaceA() instanceof PrimeA);
        Assert.assertSame(a, instance.getTypeA());
        Assert.assertNotNull(instance.getInterfaceB());
        Assert.assertTrue(instance.getInterfaceB() instanceof PrimeB);
        Assert.assertSame(b, instance.getTypeB());
        
        // also verify memoization
        Assert.assertSame(instance, r.getInstance(TypeC.class));
        
        Node<Pair<Satisfaction, CachePolicy>> resolvedRoot = r.getSolver().getGraph().getOutgoingEdge(r.getSolver().getRootNode(), rootDesire).getTail();
        Assert.assertEquals(5, r.getSolver().getGraph().getOutgoingEdges(resolvedRoot).size());
        
        Map<InjectionPoint, Node<Pair<Satisfaction, CachePolicy>>> deps = new HashMap<InjectionPoint, Node<Pair<Satisfaction, CachePolicy>>>();
        for (Edge<Pair<Satisfaction, CachePolicy>, Desire> e: r.getSolver().getGraph().getOutgoingEdges(resolvedRoot)) {
            ReflectionDesire d = (ReflectionDesire) e.getLabel();
            
            if (d.getInjectionPoint().equals(TypeC.CONSTRUCTOR)) {
                // ParameterA was set to 10
                Assert.assertFalse(deps.containsKey(TypeC.CONSTRUCTOR));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof InstanceSatisfaction);
                Assert.assertEquals(10, ((InstanceSatisfaction) e.getTail().getLabel().getKey()).getInstance());
                deps.put(TypeC.CONSTRUCTOR, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_A)) {
                // An InterfaceA has been bound to PrimeA
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_A));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof ClassSatisfaction);
                Assert.assertEquals(PrimeA.class, e.getTail().getLabel().getKey().getErasedType());
                deps.put(TypeC.INTERFACE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_A)) {
                // CycleA TypeA has been bound to an instance
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_A));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof InstanceSatisfaction);
                Assert.assertSame(a, ((InstanceSatisfaction) e.getTail().getLabel().getKey()).getInstance());
                deps.put(TypeC.TYPE_A, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.INTERFACE_B)) {
                // RoleE has been bound to PrimeB
                Assert.assertFalse(deps.containsKey(TypeC.INTERFACE_B));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof ClassSatisfaction);
                Assert.assertEquals(PrimeB.class, e.getTail().getLabel().getKey().getErasedType());
                deps.put(TypeC.INTERFACE_B, e.getTail());
            } else if (d.getInjectionPoint().equals(TypeC.TYPE_B)) {
                // TypeB has been bound to an instance
                Assert.assertFalse(deps.containsKey(TypeC.TYPE_B));
                Assert.assertTrue(e.getTail().getLabel().getKey() instanceof InstanceSatisfaction);
                Assert.assertSame(b, ((InstanceSatisfaction) e.getTail().getLabel().getKey()).getInstance());
                deps.put(TypeC.TYPE_B, e.getTail());
            } else {
                Assert.fail();
            }
        }
        
        // verify that all injection points were tested
        Assert.assertTrue(deps.containsKey(TypeC.CONSTRUCTOR));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_A));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_A));
        Assert.assertTrue(deps.containsKey(TypeC.TYPE_B));
        Assert.assertTrue(deps.containsKey(TypeC.INTERFACE_B));
    }
    
    public static class PrimeA implements InterfaceA {
        
    }
    
    public static class PrimeB implements InterfaceB {
        
    }
}
