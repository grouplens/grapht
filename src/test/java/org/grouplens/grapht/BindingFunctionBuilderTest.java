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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.solver.BindRule;
import org.grouplens.grapht.solver.BindRules;
import org.grouplens.grapht.solver.RuleBasedBindingFunction;
import org.grouplens.grapht.reflect.CachePolicy;
import org.grouplens.grapht.reflect.Satisfactions;
import org.grouplens.grapht.context.ContextElements;
import org.grouplens.grapht.context.ContextMatcher;
import org.grouplens.grapht.context.ContextPattern;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.grapht.reflect.internal.types.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Provider;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class BindingFunctionBuilderTest {
    @Test
    public void testCachePolicy() throws Exception {
        doCachePolicyTest(CachePolicy.MEMOIZE);
        doCachePolicyTest(CachePolicy.NEW_INSTANCE);
        doCachePolicyTest(CachePolicy.NO_PREFERENCE);
    }
    
    private void doCachePolicyTest(CachePolicy expectedPolicy) throws Exception {
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);
        
        if (expectedPolicy.equals(CachePolicy.MEMOIZE)) {
            builder.getRootContext().bind(InterfaceA.class).shared().to(TypeA.class);
        } else if (expectedPolicy.equals(CachePolicy.NEW_INSTANCE)) {
            builder.getRootContext().bind(InterfaceA.class).unshared().to(TypeA.class);
        } else {
            builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        }
        
        // expected
        ListMultimap<ContextMatcher,BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeA.class), expectedPolicy, false));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToType() throws Exception {
        // Test that the fluent api creates type-to-type bind rules in 
        // the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        
        // expected
        ListMultimap<ContextMatcher,BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeA.class), CachePolicy.NO_PREFERENCE, false));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToInstance() throws Exception {
        // Test that the fluent api creates type-to-instance bind rules
        // in the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        TypeA a = new TypeA();
        builder.getRootContext().bind(InterfaceA.class).to(a);
        
        // expected
        ListMultimap<ContextMatcher,BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.instance(a), CachePolicy.NO_PREFERENCE, true));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToProviderType() throws Exception {
        // Test that the fluent api creates type-to-provider type bind rules
        // in the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).toProvider(ProviderA.class);
        
        // expected
        ListMultimap<ContextMatcher,BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.providerType(ProviderA.class), CachePolicy.NO_PREFERENCE, true));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindToProviderInstance() throws Exception {
        // Test that the fluent api creates type-to-provider instance bind rules
        // in the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        ProviderA pa = new ProviderA();
        builder.getRootContext().bind(InterfaceA.class).toProvider(pa);
        
        // expected
        ListMultimap<ContextMatcher,BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.providerInstance(pa), CachePolicy.NO_PREFERENCE, true));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }

    @Test
    public void testBindToSatisfaction() throws Exception {
        // Test that the fluent api creates type-to-type bind rules in
        // the root context
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).toSatisfaction(Satisfactions.type(TypeA.class));

        // expected
        ListMultimap<ContextMatcher,BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeA.class), CachePolicy.NO_PREFERENCE, true));

        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBindToWrongProvider() throws Exception {
        // Test that we get an exception when binding to a provider of an incompatible type
        // generics prevent this, but groovy bypasses it
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);
        try {
            builder.getRootContext()
                   .bind((Class) InterfaceA.class)
                   .toProvider(ProviderC.class);
            fail("binding to incompatible provider should throw exception");
        } catch (InvalidBindingException e) {
            /* expected */
        }
    }

    @SuppressWarnings("unchecked")
    @Ignore("currently throws IllegalArgument for generic provider")
    @Test
    public void testBindToBadProvider() throws Exception {
        // Test that we get an exception when binding to a provider of an overly generic type
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);
        try {
            builder.getRootContext()
                   .bind((Class) InputStream.class)
                    .toProvider(new InstanceProvider("foo"));
            fail("binding to bad provider should throw exception");
        } catch (InvalidBindingException e) {
            /* expected */
        }
    }
    
    @Test
    public void testInjectorContextSpecificBindRules() throws Exception {
        // Test that using contexts with the fluent api properly restricts
        // created bind rules
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        builder.getRootContext().in(TypeC.class).bind(InterfaceA.class).to(TypeB.class);
        builder.getRootContext().in(RoleD.class, TypeC.class).bind(InterfaceB.class).to(TypeB.class);
        
        // expected
        ListMultimap<ContextMatcher, BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeA.class), CachePolicy.NO_PREFERENCE, false));

        expected.put(ContextPattern.subsequence(ContextElements.matchType(TypeC.class, Qualifiers.matchDefault())),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeB.class), CachePolicy.NO_PREFERENCE, false));
        expected.put(ContextPattern.subsequence(ContextElements.matchType(TypeC.class, Qualifiers.match(RoleD.class))),
                     BindRules.toSatisfaction(InterfaceB.class, Qualifiers.matchDefault(), Satisfactions.type(TypeB.class), CachePolicy.NO_PREFERENCE, false));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testFinalBindRule() throws Exception {
        // Test that type-to-type bind rules are properly terminated
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class, false);
        
        // expected
        ListMultimap<ContextMatcher, BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeA.class), CachePolicy.NO_PREFERENCE, true));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testAnnotatedBindings() throws Exception {
        // Test that bind rules properly record the qualifier they're bound with
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).withQualifier(RoleD.class).to(TypeA.class);
        
        // expected
        ListMultimap<ContextMatcher, BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.match(RoleD.class), Satisfactions.type(TypeA.class), CachePolicy.NO_PREFERENCE, false));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }

    @Test
    public void testAnyQualifierBindings() throws Exception {
        // Test that bind rules properly record the qualifier they're bound with
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(InterfaceA.class).withAnyQualifier().to(TypeA.class);

        // expected
        ListMultimap<ContextMatcher, BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchAny(), Satisfactions.type(TypeA.class), CachePolicy.NO_PREFERENCE, false));

        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testNamedBindings() throws Exception {
        // Test that bind rules properly record the name they're bound with
        BindingFunctionBuilder builder = new BindingFunctionBuilder(false);

        builder.getRootContext().bind(String.class).withQualifier(Names.named("test1")).to("hello world");
        
        // expected
        ListMultimap<ContextMatcher, BindRule> expected = ArrayListMultimap.create();
        expected.put(ContextPattern.any(),
                     BindRules.toSatisfaction(String.class, Qualifiers.match(Names.named("test1")), Satisfactions.instance("hello world"), CachePolicy.NO_PREFERENCE, true));
        
        assertEqualBindings(expected, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
    }
    
    @Test
    public void testBindRuleGeneration() throws Exception {
        // Test that bind rules are properly generated
        BindingFunctionBuilder builder = new BindingFunctionBuilder(true);

        builder.getRootContext().bind(TypeA.class).to(TypeBp.class);
        
        // expected
        ListMultimap<ContextMatcher, BindRule> explicit = ArrayListMultimap.create();
        explicit.put(ContextPattern.any(),
                     BindRules.toSatisfaction(TypeA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        ListMultimap<ContextMatcher, BindRule> superTypes = ArrayListMultimap.create();
        superTypes.put(ContextPattern.any(),
                       BindRules.toSatisfaction(InterfaceA.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        ListMultimap<ContextMatcher, BindRule> interTypes = ArrayListMultimap.create();
        ContextMatcher m = ContextPattern.any();
        interTypes.put(m, BindRules.toSatisfaction(TypeB.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        interTypes.put(m, BindRules.toSatisfaction(TypeBp.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        
        assertEqualBindings(explicit, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
        assertEqualBindings(superTypes, ((RuleBasedBindingFunction) builder.build(RuleSet.SUPER_TYPES)).getRules());
        assertEqualBindings(interTypes, ((RuleBasedBindingFunction) builder.build(RuleSet.INTERMEDIATE_TYPES)).getRules());
    }
    
    @Test
    public void testBindRuleGenerationExcludesDefault() throws Exception {
        // Test that bind rules are properly generated, and that
        // customized default types are ignored
        BindingFunctionBuilder builder = new BindingFunctionBuilder(true);
        builder.addDefaultExclusion(TypeA.class); // this causes TypeA and InterfaceA to be excluded
        
        builder.getRootContext().bind(TypeB.class).to(TypeBp.class);
        
        // expected
        ListMultimap<ContextMatcher, BindRule> explicit = ArrayListMultimap.create();
        explicit.put(ContextPattern.any(),
                     BindRules.toSatisfaction(TypeB.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        ListMultimap<ContextMatcher, BindRule> interTypes = ArrayListMultimap.create();
        interTypes.put(ContextPattern.any(),
                       BindRules.toSatisfaction(TypeBp.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        ListMultimap<ContextMatcher, BindRule> superTypes = ArrayListMultimap.create();
        superTypes.put(ContextPattern.any(),
                       BindRules.toSatisfaction(InterfaceB.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        
        assertEqualBindings(explicit, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
        assertEqualBindings(superTypes, ((RuleBasedBindingFunction) builder.build(RuleSet.SUPER_TYPES)).getRules());
        assertEqualBindings(interTypes, ((RuleBasedBindingFunction) builder.build(RuleSet.INTERMEDIATE_TYPES)).getRules());
    }
    
    @Test
    public void testBindRuleGenerationWithBindingExclude() throws Exception {
        // Test that bind rules are properly generated, taking into
        // account per-binding exclusions
        BindingFunctionBuilder builder = new BindingFunctionBuilder(true);
        
        builder.getRootContext().bind(TypeB.class).exclude(TypeA.class).to(TypeBp.class);
        
        // expected
        ListMultimap<ContextMatcher, BindRule> explicit = ArrayListMultimap.create();
        explicit.put(ContextPattern.any(),
                     BindRules.toSatisfaction(TypeB.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        ListMultimap<ContextMatcher, BindRule> interTypes = ArrayListMultimap.create();
        interTypes.put(ContextPattern.any(),
                       BindRules.toSatisfaction(TypeBp.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));
        ListMultimap<ContextMatcher, BindRule> superTypes = ArrayListMultimap.create();
        superTypes.put(ContextPattern.any(),
                       BindRules.toSatisfaction(InterfaceB.class, Qualifiers.matchDefault(), Satisfactions.type(TypeBp.class), CachePolicy.NO_PREFERENCE, false));

        assertEqualBindings(explicit, ((RuleBasedBindingFunction) builder.build(RuleSet.EXPLICIT)).getRules());
        assertEqualBindings(superTypes, ((RuleBasedBindingFunction) builder.build(RuleSet.SUPER_TYPES)).getRules());
        assertEqualBindings(interTypes, ((RuleBasedBindingFunction) builder.build(RuleSet.INTERMEDIATE_TYPES)).getRules());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRejectInvalidBinding() {
        BindingFunctionBuilder builder = new BindingFunctionBuilder(true);
        // need to go to raw types so we don't get type-check errors
        try {
            builder.getRootContext().bind((Class) OutputStream.class).to(String.class);
            fail("binding should have thrown an exception");
        } catch (InvalidBindingException e) {
            /* no-op */
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRejectInvalidInstanceBinding() {
        BindingFunctionBuilder builder = new BindingFunctionBuilder(true);
        // need to go to raw types so we don't get type-check errors
        try {
            builder.getRootContext().bind((Class) OutputStream.class).to("wombat");
            fail("binding should have thrown an exception");
        } catch (InvalidBindingException e) {
            /* no-op */
        }
    }
    
    private void assertEqualBindings(ListMultimap<ContextMatcher, BindRule> expected, ListMultimap<ContextMatcher, BindRule> actual) {
        // This special assert is needed because the collection interface doesn't specify
        // equality, but we want it to behave like set equality
        Assert.assertEquals(expected.size(), actual.size());
        SetMultimap eset = HashMultimap.create(expected);
        SetMultimap aset = HashMultimap.create(actual);
        assertEquals(eset, aset);
    }
    
    // TypeBp is a TypeB, TypeA, InterfaceB, and InterfaceA
    public static class TypeBp extends TypeB { }

    public static class InstanceProvider<T> implements Provider<T> {
        private final T instance;

        public InstanceProvider(T obj) {
            instance = obj;
        }

        @Override
        public T get() {
            return instance;
        }
    }
}
