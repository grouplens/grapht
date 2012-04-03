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

import org.grouplens.grapht.InjectorConfiguration;
import org.grouplens.grapht.InjectorConfigurationBuilder;
import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.grapht.spi.reflect.types.InterfaceA;
import org.grouplens.grapht.spi.reflect.types.InterfaceB;
import org.grouplens.grapht.spi.reflect.types.ProviderA;
import org.grouplens.grapht.spi.reflect.types.RoleE;
import org.grouplens.grapht.spi.reflect.types.TypeA;
import org.grouplens.grapht.spi.reflect.types.TypeB;
import org.grouplens.grapht.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InjectorConfigurationBuilderTest {
    private InjectSPI spi;
    
    @Before
    public void setup() {
        spi = new ReflectionInjectSPI();
    }
    
    @Test
    public void testBindToType() throws Exception {
        // Test that the fluent api creates type-to-type bind rules in 
        // the root context
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(null, InterfaceA.class, TypeA.class, 0, false)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testBindToInstance() throws Exception {
        // Test that the fluent api creates type-to-instance bind rules
        // in the root context
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        TypeA a = new TypeA();
        builder.getRootContext().bind(InterfaceA.class).to(a);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindInstance(null, InterfaceA.class, a, 0)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testBindToProviderType() throws Exception {
        // Test that the fluent api creates type-to-provider type bind rules
        // in the root context
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).toProvider(ProviderA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindProvider(null, InterfaceA.class, ProviderA.class, 0)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testBindToProviderInstance() throws Exception {
        // Test that the fluent api creates type-to-provider instance bind rules
        // in the root context
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        ProviderA pa = new ProviderA();
        builder.getRootContext().bind(InterfaceA.class).toProvider(pa);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindProvider(null, InterfaceA.class, pa, 0)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testInjectorContextSpecificBindRules() throws Exception {
        // Test that using contexts with the fluent api properly restricts
        // created bind rules
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        builder.getRootContext().in(TypeC.class).bind(InterfaceA.class).to(TypeB.class);
        builder.getRootContext().in(RoleE.class, TypeC.class).bind(InterfaceB.class).to(TypeB.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(null, InterfaceA.class, TypeA.class, 0, false)));
        expected.put(new ContextChain(Arrays.asList(spi.context(null, TypeC.class))),
                     Arrays.asList(spi.bindType(null, InterfaceA.class, TypeB.class, 0, false)));
        expected.put(new ContextChain(Arrays.asList(spi.context(spi.qualifier(RoleE.class), TypeC.class))),
                     Arrays.asList(spi.bindType(null, InterfaceB.class, TypeB.class, 0, false)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testFinalBindRule() throws Exception {
        // Test that type-to-type bind rules are properly terminated
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).finalBinding().to(TypeA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(null, InterfaceA.class, TypeA.class, 0, true)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testAnnotatedBindings() throws Exception {
        // Test that bind rules properly record the qualifier they're bound with
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        builder.getRootContext().bind(InterfaceA.class).withQualifier(RoleE.class).to(TypeA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(spi.qualifier(RoleE.class), InterfaceA.class, TypeA.class, 0, false)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testNamedBindings() throws Exception {
        // Test that bind rules properly record the name they're bound with
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, false);

        builder.getRootContext().bind(String.class).withName("test1").to("hello world");
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindInstance(spi.qualifier("test1"), String.class, "hello world", 0)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testBindRuleGeneration() throws Exception {
        // Test that bind rules are properly generated
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, true);

        builder.getRootContext().bind(TypeA.class).to(TypeBp.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()),
                     Arrays.asList(spi.bindType(null, TypeA.class, TypeBp.class, BindRule.MANUAL_BIND_RULE, false),
                                   spi.bindType(null, InterfaceA.class, TypeBp.class, BindRule.SECOND_TIER_GENERATED_BIND_RULE, false),
                                   spi.bindType(null, TypeB.class, TypeBp.class, BindRule.FIRST_TIER_GENERATED_BIND_RULE, false),
                                   spi.bindType(null, TypeBp.class, TypeBp.class, BindRule.FIRST_TIER_GENERATED_BIND_RULE, false)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testBindRuleGenerationExcludesDefault() throws Exception {
        // Test that bind rules are properly generated, and that
        // customized default types are ignored
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, true);
        builder.addDefaultExclusion(TypeA.class); // this causes TypeA and InterfaceA to be excluded
        
        builder.getRootContext().bind(TypeB.class).to(TypeBp.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()),
                     Arrays.asList(spi.bindType(null, TypeB.class, TypeBp.class, BindRule.MANUAL_BIND_RULE, false),
                                   spi.bindType(null, TypeBp.class, TypeBp.class, BindRule.FIRST_TIER_GENERATED_BIND_RULE, false),
                                   spi.bindType(null, InterfaceB.class, TypeBp.class, BindRule.SECOND_TIER_GENERATED_BIND_RULE, false)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    @Test
    public void testBindRuleGenerationWithBindingExclude() throws Exception {
        // Test that bind rules are properly generated, taking into
        // account per-binding exclusions
        InjectorConfigurationBuilder builder = new InjectorConfigurationBuilder(spi, true);
        
        builder.getRootContext().bind(TypeB.class).exclude(TypeA.class).to(TypeBp.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()),
                     Arrays.asList(spi.bindType(null, TypeB.class, TypeBp.class, BindRule.MANUAL_BIND_RULE, false),
                                   spi.bindType(null, TypeBp.class, TypeBp.class, BindRule.FIRST_TIER_GENERATED_BIND_RULE, false),
                                   spi.bindType(null, InterfaceB.class, TypeBp.class, BindRule.SECOND_TIER_GENERATED_BIND_RULE, false)));
        
        assertEqualBindings(expected, config.getBindRules());
    }
    
    private void assertEqualBindings(Map<ContextChain, Collection<? extends BindRule>> expected, Map<ContextChain, Collection<? extends BindRule>> actual) {
        // This special assert is needed because the collection interface doesn't specify
        // equality, but we want it to behave like set equality
        Assert.assertEquals(expected.size(), actual.size());
        for (Entry<ContextChain, Collection<? extends BindRule>> e: expected.entrySet()) {
            Set<BindRule> s1 = new HashSet<BindRule>(e.getValue());
            Set<BindRule> s2 = new HashSet<BindRule>(actual.get(e.getKey()));
            Assert.assertEquals(s1, s2);
        }
    }
    
    // TypeBp is a TypeB, TypeA, InterfaceB, and InterfaceA
    public static class TypeBp extends TypeB { }
}
