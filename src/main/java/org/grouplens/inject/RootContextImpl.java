package org.grouplens.inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.InjectSPI;

class RootContextImpl implements RootContext {
    private final ContextImpl delegate;
    private final Set<Class<?>> defaultExcludes;
    
    private final Map<ContextChain, Collection<BindRule>> bindRules;
    
    public RootContextImpl(InjectSPI spi) {
        delegate = new ContextImpl(spi, this, new ContextChain(new ArrayList<ContextMatcher>()));
        defaultExcludes = new HashSet<Class<?>>();
        bindRules = new HashMap<ContextChain, Collection<BindRule>>();
    }
    
    public void addBindRule(ContextChain context, BindRule rule) {
        Collection<BindRule> inContext = bindRules.get(context);
        if (inContext == null) {
            inContext = new ArrayList<BindRule>();
            bindRules.put(context, inContext);
        }
        
        inContext.add(rule);
    }
    
    @Override
    public void addDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.add(type);
    }

    @Override
    public void removeDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.remove(type);
    }

    @Override
    public Set<Class<?>> getDefaultExclusions() {
        return Collections.unmodifiableSet(defaultExcludes);
    }

    @Override
    public Map<ContextChain, Collection<? extends BindRule>> bindRules() {
        Map<ContextChain, Collection<? extends BindRule>> copy = new HashMap<ContextChain, Collection<? extends BindRule>>(bindRules);
        return copy;
    }

    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return delegate.bind(type);
    }

    @Override
    public <T> Binding<T> bind(Class<T> type, Class<?>... otherTypes) {
        return delegate.bind(type, otherTypes);
    }

    @Override
    public Context in(Class<?> type) {
        return delegate.in(type);
    }

    @Override
    public Context in(@Nullable Class<? extends Annotation> role, Class<?> type) {
        return delegate.in(role, type);
    }
}
