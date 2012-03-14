package org.grouplens.inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;
import org.grouplens.inject.spi.reflect.types.InterfaceA;
import org.grouplens.inject.spi.reflect.types.InterfaceB;
import org.grouplens.inject.spi.reflect.types.ProviderA;
import org.grouplens.inject.spi.reflect.types.RoleE;
import org.grouplens.inject.spi.reflect.types.TypeA;
import org.grouplens.inject.spi.reflect.types.TypeB;
import org.grouplens.inject.spi.reflect.types.TypeC;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class InjectorConfigurationBuilderTest {
    private InjectSPI spi;
    private InjectorConfigurationBuilder builder;
    
    @Before
    public void setup() {
        spi = new ReflectionInjectSPI();
        builder = new InjectorConfigurationBuilder(spi);
    }
    
    @Test
    public void testBindToType() throws Exception {
        // Test that the fluent api creates type-to-type bind rules in 
        // the root context
        builder.getRootContext().bind(InterfaceA.class).to(TypeA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(null, InterfaceA.class, TypeA.class, 0, false)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    public void testBindToInstance() throws Exception {
        // Test that the fluent api creates type-to-instance bind rules
        // in the root context
        TypeA a = new TypeA();
        builder.getRootContext().bind(InterfaceA.class).to(a);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindInstance(null, InterfaceA.class, a, 0)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    public void testBindToProviderType() throws Exception {
        // Test that the fluent api creates type-to-provider type bind rules
        // in the root context
        builder.getRootContext().bind(InterfaceA.class).toProvider(ProviderA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindProvider(null, InterfaceA.class, ProviderA.class, 0)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    public void testBindToProviderInstance() throws Exception {
        // Test that the fluent api creates type-to-provider instance bind rules
        // in the root context
        ProviderA pa = new ProviderA();
        builder.getRootContext().bind(InterfaceA.class).toProvider(pa);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindProvider(null, InterfaceA.class, pa, 0)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    public void testInjectorContextSpecificBindRules() throws Exception {
        // Test that using contexts with the fluent api properly restricts
        // created bind rules
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
        expected.put(new ContextChain(Arrays.asList(spi.context(RoleE.class, TypeC.class))),
                     Arrays.asList(spi.bindType(null, InterfaceB.class, TypeB.class, 0, false)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    public void testFinalBindRule() throws Exception {
        // Test that type-to-type bind rules are properly terminated
        builder.getRootContext().bind(InterfaceA.class).finalBinding().to(TypeA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(null, InterfaceA.class, TypeA.class, 0, true)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    public void testInjectorRoleBindings() throws Exception {
        // Test that bind rules properly record the role their bound with
        builder.getRootContext().bind(InterfaceA.class).withRole(RoleE.class).to(TypeA.class);
        InjectorConfiguration config = builder.build();
        
        // expected
        Map<ContextChain, Collection<? extends BindRule>> expected = new HashMap<ContextChain, Collection<? extends BindRule>>();
        expected.put(new ContextChain(new ArrayList<ContextMatcher>()), 
                     Arrays.asList(spi.bindType(RoleE.class, InterfaceA.class, TypeA.class, 0, false)));
        
        Assert.assertEquals(expected, config.getBindRules());
    }
    
    @Test
    @Ignore
    public void testBindRuleGeneration() throws Exception {
        // Test that bind rules are properly generated
        Assert.fail();
    }
    
    @Test
    @Ignore
    public void testBindRuleGenerationExcludesDefault() throws Exception {
        // Test that bind rules are properly generated, and that
        // customized default types are ignored
        Assert.fail();
    }
    
    @Test
    @Ignore
    public void testBindRuleGenerationWithBindingExclude() throws Exception {
        // Test that bind rules are properly generated, taking into
        // account per-binding exclusions
        Assert.fail();
    }
}
