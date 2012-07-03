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

import java.io.Externalizable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.grouplens.grapht.solver.BindRule;
import org.grouplens.grapht.solver.BindingFunction;
import org.grouplens.grapht.solver.RuleBasedBindingFunction;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;

/**
 * BindingFunctionBuilder provides a convenient access to the fluent API and
 * converts calls to {@link Context} and {@link Binding} methods into multiple
 * {@link BindingFunction BindingFunctions}.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class BindingFunctionBuilder implements Cloneable {
    /**
     * BindingFunctionBuilder generates three binding functions at separate
     * priorities.
     */
    public static enum RuleSet {
        /**
         * Rule set for the explicitly configured rules with the fluent API.
         */
        EXPLICIT,
        /**
         * Rule set for the intermediate types between a source type (
         * {@link Context#bind(Class)}) and the target type (
         * {@link Binding#to(Class)})
         */
        INTERMEDIATE_TYPES,
        /**
         * Rule set for the super types of the source types of bindings (e.g.
         * {@link Context#bind(Class)})
         */
        SUPER_TYPES
    }
    
    private final InjectSPI spi;
    private final Context root;
    
    private final Set<Class<?>> defaultExcludes;
    private final boolean generateRules;
    
    private final Map<ContextChain, Collection<BindRule>> manualRules;
    private final Map<ContextChain, Collection<BindRule>> intermediateRules; // "generated"
    private final Map<ContextChain, Collection<BindRule>> superRules; // "generated"

    /**
     * Create a new InjectorConfigurationBuilder that uses a
     * {@link ReflectionInjectSPI} and automatically generates bind rules for
     * super and intermediate types.
     */
    public BindingFunctionBuilder() {
        this(true);
    }

    /**
     * Create a new InjectorConfigurationBuilder that uses a
     * {@link ReflectionInjectSPI}. If <tt>generateRules</tt> is true, bind
     * rules for super and intermediate types are generated. If it is false,
     * only one bind rule is created per binding.
     * 
     * @param generateRules True if additional bind rules should be generated
     */
    public BindingFunctionBuilder(boolean generateRules) {
        this(new ReflectionInjectSPI(), generateRules);
    }
    
    /**
     * Create a new InjectorConfigurationBuilder that uses the given
     * {@link InjectSPI} instance.
     * 
     * @param spi The injection service provider to use
     * @param generateRules True if additional bind rules should be generated
     *            for intermediate and super types
     * @throws NullPointerException if spi is null
     */
    public BindingFunctionBuilder(InjectSPI spi, boolean generateRules) {
        if (spi == null) {
            throw new NullPointerException("SPI cannot be null");
        }
        
        this.spi = spi;
        this.generateRules = generateRules;
        
        defaultExcludes = new HashSet<Class<?>>();
        defaultExcludes.add(Object.class);
        defaultExcludes.add(Comparable.class);
        defaultExcludes.add(Serializable.class);
        defaultExcludes.add(Externalizable.class);
        defaultExcludes.add(Cloneable.class);
        
        manualRules = new HashMap<ContextChain, Collection<BindRule>>();
        intermediateRules = new HashMap<ContextChain, Collection<BindRule>>();
        superRules = new HashMap<ContextChain, Collection<BindRule>>();
        
        root = new ContextImpl(this, new ContextChain(new ArrayList<ContextMatcher>()));
    }
    
    private BindingFunctionBuilder(BindingFunctionBuilder clone) {
        spi = clone.spi;
        generateRules = clone.generateRules;
        defaultExcludes = new HashSet<Class<?>>(clone.defaultExcludes);
        manualRules = deepCloneRules(clone.manualRules);
        intermediateRules = deepCloneRules(clone.intermediateRules);
        superRules = deepCloneRules(clone.superRules);
        root = new ContextImpl(this, new ContextChain(new ArrayList<ContextMatcher>()));
    }
    
    @Override
    public BindingFunctionBuilder clone() {
        return new BindingFunctionBuilder(this);
    }
    
    /**
     * @return True if bind rules for super and intermediate types should be
     *         generated
     */
    public boolean getGenerateRules() {
        return generateRules;
    }
    
    /**
     * @return The SPI used by this builder
     */
    public InjectSPI getSPI() {
        return spi;
    }
    
    /**
     * @return The root context managed by this builder
     */
    public Context getRootContext() {
        return root;
    }

    /**
     * Run the module's {@link Module#bind(Context) bind()} method on the root
     * context of this builder.
     * 
     * @param module The module to apply
     */
    public void applyModule(Module module) {
        module.bind(getRootContext());
    }

    /**
     * Add a type to be excluded from when generating bind rules. This does not
     * invalidate bindings that bind directly to this type.
     * 
     * @param type The type to exclude
     * @throws NullPointerException if type is null
     */
    public void addDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.add(type);
    }

    /**
     * Remove a type that is currently being excluded.
     * 
     * @see #addDefaultExclusion(Class)
     * @param type The type that should no longer be excluded
     * @throws NullPointerException if type is null
     */
    public void removeDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.remove(type);
    }
    
    /**
     * Return the built BindingFunction for the given RuleSet.
     * 
     * @param set
     * @return
     */
    public BindingFunction getFunction(RuleSet set) {
        return new RuleBasedBindingFunction(getMap(set));
    }
    
    void addBindRule(RuleSet set, ContextChain context, BindRule rule) {
        Map<ContextChain, Collection<BindRule>> map = getMap(set);
        
        Collection<BindRule> inContext = map.get(context);
        if (inContext == null) {
            inContext = new ArrayList<BindRule>();
            map.put(context, inContext);
        }
        
        inContext.add(rule);
    }

    Set<Class<?>> getDefaultExclusions() {
        return Collections.unmodifiableSet(defaultExcludes);
    }
    
    private static Map<ContextChain, Collection<BindRule>> deepCloneRules(Map<ContextChain, Collection<BindRule>> bindRules) {
        Map<ContextChain, Collection<BindRule>> rules = new HashMap<ContextChain, Collection<BindRule>>();
        for (Entry<ContextChain, Collection<BindRule>> e: bindRules.entrySet()) {
            rules.put(e.getKey(), new ArrayList<BindRule>(e.getValue()));
        }
        return rules;
    }
    
    private Map<ContextChain, Collection<BindRule>> getMap(RuleSet set) {
        switch(set) {
        case EXPLICIT:
            return manualRules;
        case INTERMEDIATE_TYPES:
            return intermediateRules;
        case SUPER_TYPES:
            return superRules;
        default:
            throw new RuntimeException("Should not happen");
        }
    }
}
