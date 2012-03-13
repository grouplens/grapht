package org.grouplens.inject;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class InjectorConfigurationBuilderTest {

    
    @Test
    public void testBindToType() throws Exception {
        // Test that the fluent api creates type-to-type bind rules in 
        // the root context
        Assert.fail();
    }
    
    @Test
    public void testBindToInstance() throws Exception {
        // Test that the fluent api creates type-to-instance bind rules
        // in the root context
        Assert.fail();
    }
    
    @Test
    public void testBindToProviderType() throws Exception {
        // Test that the fluent api creates type-to-provider type bind rules
        // in the root context
        Assert.fail();
    }
    
    @Test
    public void testBindToProviderInstance() throws Exception {
        // Test that the fluent api creates type-to-provider instance bind rules
        // in the root context
        Assert.fail();
    }
    
    @Test
    public void testInjectorContextSpecificBindRules() throws Exception {
        // Test that using contexts with the fluent api properly restricts
        // created bind rules
        Assert.fail();
    }
    
    @Test
    public void testFinalBindRule() throws Exception {
        // Test that type-to-type bind rules are properly terminated
        Assert.fail();
    }
    
    @Test
    public void testInjectorRoleBindings() throws Exception {
        // Test that bind rules properly record the role their bound with
        Assert.fail();
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
