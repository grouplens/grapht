/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Provider;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;
import org.grouplens.inject.types.Types;

/**
 * BindingImpl is the default implementation of Binding that is used by
 * {@link InjectorConfigurationBuilder}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The bindings source's type
 */
class BindingImpl<T> implements Binding<T> {
    private final ContextImpl context;
    private final Class<T> sourceType;
    
    private final Set<Class<?>> excludeTypes;
    
    private Class<? extends Annotation> qualifier;
    private boolean terminate;
    
    private boolean bindingCompleted;
    
    public BindingImpl(ContextImpl context, Class<T> type) {
        this.context = context;
        sourceType = type;
        excludeTypes = new HashSet<Class<?>>(context.getBuilder().getDefaultExclusions());
        
        bindingCompleted = false;
        terminate = false;
    }
    
    private void validateState() {
        if (bindingCompleted) {
            throw new IllegalStateException("Binding already completed");
        }
    }
    
    @Override
    public Binding<T> withQualifier(Class<? extends Annotation> qualifier) {
        if (qualifier == null) {
            throw new NullPointerException("Qualifier cannot be null");
        }
        validateState();
        this.qualifier = qualifier;
        return this;
    }

    @Override
    public Binding<T> exclude(Class<?> exclude) {
        if (exclude == null) {
            throw new NullPointerException("Type cannot be null");
        }
        validateState();
        excludeTypes.add(exclude);
        return this;
    }
    
    @Override
    public Binding<T> finalBinding() {
        validateState();
        terminate = true;
        return this;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void to(Class<? extends T> impl) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(impl);
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindType(qualifier, (Class) e.getKey(), impl, e.getValue(), terminate);
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindType(qualifier, sourceType, impl, BindRule.MANUAL_BIND_RULE, terminate));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void to(T instance) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(instance.getClass());
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindInstance(qualifier, (Class) e.getKey(), instance, e.getValue());
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindInstance(qualifier, sourceType, instance, BindRule.MANUAL_BIND_RULE));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void toProvider(Class<? extends Provider<? extends T>> provider) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(Types.getProvidedType(provider));
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindProvider(qualifier, (Class) e.getKey(), provider, e.getValue());
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindProvider(qualifier, sourceType, provider, BindRule.MANUAL_BIND_RULE));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void toProvider(Provider<? extends T> provider) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        if (config.getGenerateRules()) {
            Map<Class<?>, Integer> bindPoints = generateBindPoints(Types.getProvidedType(provider));
            for (Entry<Class<?>, Integer> e: bindPoints.entrySet()) {
                BindRule rule = config.getSPI().bindProvider(qualifier, (Class) e.getKey(), provider, e.getValue());
                config.addBindRule(chain, rule);
            }
        } else {
            config.addBindRule(chain, config.getSPI().bindProvider(qualifier, sourceType, provider, BindRule.MANUAL_BIND_RULE));
        }
    }
    
    private Map<Class<?>, Integer> generateBindPoints(Class<?> target) {
        Map<Class<?>, Integer> bindPoints = new HashMap<Class<?>, Integer>();
        // start the recursion up the type hierarchy, starting at the target type
        recordTypes(target, bindPoints);
        return bindPoints;
    }
    
    private void recordTypes(Class<?> type, Map<Class<?>, Integer> bindPoints) {
        // check exclusions
        if (type == null || excludeTypes.contains(type)) {
            // the type is excluded, terminate recursion (this relies on Object
            // being included in the exclude set)
            return;
        }
        
        int weight;
        if (type.equals(sourceType)) {
            // type is the source type, so this is the manual rule
            weight = BindRule.MANUAL_BIND_RULE;
        } else if (sourceType.isAssignableFrom(type)) {
            // type is a subclass of the source type, and a superclass
            // of the target type
            weight = BindRule.FIRST_TIER_GENERATED_BIND_RULE;
        } else if (type.isAssignableFrom(sourceType)) {
            // type is a superclass of the source type, so it is also a superclass
            // of the target type
            weight = BindRule.SECOND_TIER_GENERATED_BIND_RULE;
        } else {
            // type is a superclass of the target type, but not of the source type
            // so we don't generate any bindings
            return;
        }
        
        // record the type's weight
        bindPoints.put(type, weight);
        
        // recurse to superclass and implemented interfaces
        // - superclass is null for Object, interfaces, and primitives
        // - interfaces holds implemented or extended interfaces depending on
        //   if the type is a class or interface
        recordTypes(type.getSuperclass(), bindPoints);
        for (Class<?> i: type.getInterfaces()) {
            recordTypes(i, bindPoints);
        }
    }
}
