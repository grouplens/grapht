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
package org.grouplens.grapht;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.solver.BindRule;
import org.grouplens.grapht.solver.RuleBasedBindingFunction;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.grapht.spi.reflect.types.InterfaceA;
import org.grouplens.grapht.spi.reflect.types.InterfaceB;
import org.grouplens.grapht.spi.reflect.types.ProviderA;
import org.grouplens.grapht.spi.reflect.types.RoleD;
import org.grouplens.grapht.spi.reflect.types.TypeA;
import org.grouplens.grapht.spi.reflect.types.TypeB;
import org.grouplens.grapht.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BindingFunctionBuilderTest {
    private InjectSPI spi;
    
    @Before
    public void setup() {
        spi = new ReflectionInjectSPI();
    }
    
    @Test
    public void testCachePolicy() throws Exception {
        doCachePolicyTest(CachePolicy.MEMOIZE);
        doCachePolicyTest(CachePolicy.NEW_INSTANCE);
        doCachePolicyTest(CachePolicy.NO_PREFERENCE);
    }
    
    private void doCachePolicyTest(CachePolicy expectedPolicy) throws Exception {
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);
        
        if (expectedPolicy.equals(CachePolicy.MEMOIZE)) {
            builder.getRootContext().bind(InterfaceA.class).shared().to(TypeA.class);
        } else if (expectedPolicy.equals(CachePolicy.NEW_INSTANCE)) {
            builder.getRootContext().bind(InterfaceA.class).unshared().to(TypeA.class);
        } else {
            builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        }
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeA.class), expectedPolicy, spi.matchAny(), false)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToType() throws Exception {
        // Test that the fluent api creates type-to-type bind rules in 
        // the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToInstance() throws Exception {
        // Test that the fluent api creates type-to-instance bind rules
        // in the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        TypeA a = new TypeA();
        builder.getRootContext().bind(InterfaceA.class).to(a);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(a), CachePolicy.NO_PREFERENCE, spi.matchAny(), true)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToProviderType() throws Exception {
        // Test that the fluent api creates type-to-provider type bind rules
        // in the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).toProvider(ProviderA.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfyWithProvider(ProviderA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), true)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToProviderInstance() throws Exception {
        // Test that the fluent api creates type-to-provider instance bind rules
        // in the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        ProviderA pa = new ProviderA();
        builder.getRootContext().bind(InterfaceA.class).toProvider(pa);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfyWithProvider(pa), CachePolicy.NO_PREFERENCE, spi.matchAny(), true)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testInjectorContextSpecificBindRules() throws Exception {
        // Test that using contexts with the fluent api properly restricts
        // created bind rules
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        builder.getRootContext().in(TypeC.class).bind(InterfaceA.class).to(TypeB.class);
        builder.getRootContext().in(RoleD.class, TypeC.class).bind(InterfaceB.class).to(TypeB.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        expected.put(new ContextChain(Arrays.asList(spi.context(spi.matchAny(), TypeC.class))),
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeB.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        expected.put(new ContextChain(Arrays.asList(spi.context(spi.match(RoleD.class), TypeC.class))),
                     Arrays.asList(new BindRule(InterfaceB.class, spi.satisfy(TypeB.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testFinalBindRule() throws Exception {
        // Test that type-to-type bind rules are properly terminated
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).finalBinding().to(TypeA.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeA.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), true)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testAnnotatedBindings() throws Exception {
        // Test that bind rules properly record the qualifier they're bound with
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).withQualifier(RoleD.class).to(TypeA.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeA.class), CachePolicy.NO_PREFERENCE, spi.match(RoleD.class), false)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testNamedBindings() throws Exception {
        // Test that bind rules properly record the name they're bound with
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, false);

        builder.getRootContext().bind(String.class).withQualifier(Names.named("test1")).to("hello world");
        
        // expected
        Map<ContextChain, Collection<BindRule>> expected = new HashMap<ContextChain, Collection<BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(new BindRule(String.class, spi.satisfy("hello world"), CachePolicy.NO_PREFERENCE, spi.match(Names.named("test1")), true)));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindRuleGeneration() throws Exception {
        // Test that bind rules are properly generated
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, true);

        builder.getRootContext().bind(TypeA.class).to(TypeBp.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> explicit = new HashMap<ContextChain, Collection<BindRule>>();
        explicit.put(new ContextChain(new ArrayList<ContextMatcher>()),
                     Arrays.asList(new BindRule(TypeA.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Map<ContextChain, Collection<BindRule>> superTypes = new HashMap<ContextChain, Collection<BindRule>>();
        superTypes.put(new ContextChain(new ArrayList<ContextMatcher>()),
                       Arrays.asList(new BindRule(InterfaceA.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Map<ContextChain, Collection<BindRule>> interTypes = new HashMap<ContextChain, Collection<BindRule>>();
        interTypes.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                       Arrays.asList(new BindRule(TypeB.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false),
                                     new BindRule(TypeBp.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        
        assertEqualBindings(explicit, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
        assertEqualBindings(superTypes, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.SUPER_TYPES)).getRules());
        assertEqualBindings(interTypes, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.INTERMEDIATE_TYPES)).getRules());
    }
    
    @Test
    public void testBindRuleGenerationExcludesDefault() throws Exception {
        // Test that bind rules are properly generated, and that
        // customized default types are ignored
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, true);
        builder.addDefaultExclusion(TypeA.class); // this causes TypeA and InterfaceA to be excluded
        
        builder.getRootContext().bind(TypeB.class).to(TypeBp.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> explicit = new HashMap<ContextChain, Collection<BindRule>>();
        explicit.put(new ContextChain(new ArrayList<ContextMatcher>()),
                     Arrays.asList(new BindRule(TypeB.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Map<ContextChain, Collection<BindRule>> interTypes = new HashMap<ContextChain, Collection<BindRule>>();
        interTypes.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                       Arrays.asList(new BindRule(TypeBp.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Map<ContextChain, Collection<BindRule>> superTypes = new HashMap<ContextChain, Collection<BindRule>>();
        superTypes.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                       Arrays.asList(new BindRule(InterfaceB.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        
        assertEqualBindings(explicit, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
        assertEqualBindings(superTypes, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.SUPER_TYPES)).getRules());
        assertEqualBindings(interTypes, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.INTERMEDIATE_TYPES)).getRules());
    }
    
    @Test
    public void testBindRuleGenerationWithBindingExclude() throws Exception {
        // Test that bind rules are properly generated, taking into
        // account per-binding exclusions
        BindingFunctionBuilder builder = new BindingFunctionBuilder(spi, true);
        
        builder.getRootContext().bind(TypeB.class).exclude(TypeA.class).to(TypeBp.class);
        
        // expected
        Map<ContextChain, Collection<BindRule>> explicit = new HashMap<ContextChain, Collection<BindRule>>();
        explicit.put(new ContextChain(new ArrayList<ContextMatcher>()),
                     Arrays.asList(new BindRule(TypeB.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Map<ContextChain, Collection<BindRule>> interTypes = new HashMap<ContextChain, Collection<BindRule>>();
        interTypes.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                       Arrays.asList(new BindRule(TypeBp.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));
        Map<ContextChain, Collection<BindRule>> superTypes = new HashMap<ContextChain, Collection<BindRule>>();
        superTypes.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                       Arrays.asList(new BindRule(InterfaceB.class, spi.satisfy(TypeBp.class), CachePolicy.NO_PREFERENCE, spi.matchAny(), false)));

        assertEqualBindings(explicit, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.EXPLICIT)).getRules());
        assertEqualBindings(superTypes, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.SUPER_TYPES)).getRules());
        assertEqualBindings(interTypes, ((RuleBasedBindingFunction) builder.getFunction(RuleSet.INTERMEDIATE_TYPES)).getRules());
    }
    
    private void assertEqualBindings(Map<ContextChain, Collection<BindRule>> expected, Map<ContextChain, Collection<BindRule>> actual) {
        // This special assert is needed because the collection interface doesn't specify
        // equality, but we want it to behave like set equality
        Assert.assertEquals(expected.size(), actual.size());
        for (Entry<ContextChain, Collection<BindRule>> e: expected.entrySet()) {
            Set<BindRule> s1 = new HashSet<BindRule>(e.getValue());
            Set<BindRule> s2 = new HashSet<BindRule>(actual.get(e.getKey()));
            Assert.assertEquals(s1, s2);
        }
    }
    
    // TypeBp is a TypeB, TypeA, InterfaceB, and InterfaceA
    public static class TypeBp extends TypeB { }
}
