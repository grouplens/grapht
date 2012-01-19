package org.grouplens.inject;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;

public interface RootContext extends Context {
    void addDefaultExclusion(Class<?> type);
    
    void removeDefaultExclusion(Class<?> type);
    
    Set<Class<?>> getDefaultExclusions();
    
    Map<ContextChain, Collection<? extends BindRule>> bindRules();
}
