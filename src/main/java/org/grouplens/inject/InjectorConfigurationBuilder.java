package org.grouplens.inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.builder.Builder;
import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;

public class InjectorConfigurationBuilder implements Builder<InjectorConfiguration> {
    private RootContextImpl root;
    
    public Context getRootContext() {
        return root;
    }
    
    public void addModule(Module module) {
        module.bind(getRootContext());
    }
    
    @Override
    public InjectorConfiguration build() {
        Map<ContextChain, Collection<BindRule>> toCopy = root.getBindRules();
        // make a deep copy of the bind rules, since the map's key set can change
        // and the collection of bind rules can change
        final Map<ContextChain, Collection<? extends BindRule>> rules = new HashMap<ContextChain, Collection<? extends BindRule>>();
        for (Entry<ContextChain, Collection<BindRule>> e: toCopy.entrySet()) {
            rules.put(e.getKey(), new ArrayList<BindRule>(e.getValue()));
        }
        
        return new InjectorConfiguration() {
            @Override
            public Map<ContextChain, Collection<? extends BindRule>> getBindRules() {
                return rules;
            }
        };
    }
}
