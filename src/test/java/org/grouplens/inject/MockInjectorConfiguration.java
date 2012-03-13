package org.grouplens.inject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;

/**
 * MockInjectorConfiguration wraps an existing map of bind rules.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockInjectorConfiguration implements InjectorConfiguration {
    private final Map<ContextChain, Collection<? extends BindRule>> rules;
    
    public MockInjectorConfiguration(Map<ContextChain, Collection<? extends BindRule>> rules) {
        this.rules = Collections.unmodifiableMap(rules);
    }
    
    @Override
    public Map<ContextChain, Collection<? extends BindRule>> getBindRules() {
        return rules;
    }
}
