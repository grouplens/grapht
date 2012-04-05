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

import org.grouplens.grapht.spi.BindRule;
import org.grouplens.grapht.spi.ContextChain;
import org.grouplens.grapht.spi.ContextMatcher;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;

/**
 * <p>
 * InjectorConfigurationBuilder is a Builder that creates
 * InjectorConfigurations. This uses its own implementations of {@link Context}
 * and {@link Binding} to accumulate {@link BindRule BindRules}. For simple
 * applications, {@link InjectorBuilder} is the recommended entry point.
 * InjectorConfigurationBuilder is useful for different implementations of
 * Injector that only need to change the inject behavior, but do not need to
 * modify configuration.
 * <p>
 * The fluent API provided by the InjectorConfigurationBuilder will by default
 * generate additional {@link BindRule BindRules} when {@link Binding bindings}
 * are completed. A BindRule has a source type and a target type; the source
 * represents the type declared at the injection point and the target type is
 * the satisfying implementation. The fluent API generates bindings for the
 * super types of the source type, and the intermediate types between the source
 * and target. This allows for much simpler binding configurations that still
 * allow types to have narrower dependencies.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class InjectorConfigurationBuilder implements Cloneable {
    private final InjectSPI spi;
    private final Context root;
    
    private final Set<Class<?>> defaultExcludes;
    private final Map<ContextChain, Collection<BindRule>> bindRules;
    private final boolean generateRules;

    /**
     * Create a new InjectorConfigurationBuilder that uses a
     * {@link ReflectionInjectSPI} and automatically generates bind rules for
     * super and intermediate types.
     */
    public InjectorConfigurationBuilder() {
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
    public InjectorConfigurationBuilder(boolean generateRules) {
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
    public InjectorConfigurationBuilder(InjectSPI spi, boolean generateRules) {
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
        
        bindRules = new HashMap<ContextChain, Collection<BindRule>>();
        
        root = new ContextImpl(this, new ContextChain(new ArrayList<ContextMatcher>()));
    }
    
    private InjectorConfigurationBuilder(InjectorConfigurationBuilder clone) {
        spi = clone.spi;
        generateRules = clone.generateRules;
        defaultExcludes = new HashSet<Class<?>>(clone.defaultExcludes);
        bindRules = clone.getBindRulesDeepClone();
        root = new ContextImpl(this, new ContextChain(new ArrayList<ContextMatcher>()));
    }
    
    @Override
    public InjectorConfigurationBuilder clone() {
        return new InjectorConfigurationBuilder(this);
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
    
    void addBindRule(ContextChain context, BindRule rule) {
        Collection<BindRule> inContext = bindRules.get(context);
        if (inContext == null) {
            inContext = new ArrayList<BindRule>();
            bindRules.put(context, inContext);
        }
        
        inContext.add(rule);
    }

    Set<Class<?>> getDefaultExclusions() {
        return Collections.unmodifiableSet(defaultExcludes);
    }
    
    private Map<ContextChain, Collection<BindRule>> getBindRulesDeepClone() {
        Map<ContextChain, Collection<BindRule>> rules = new HashMap<ContextChain, Collection<BindRule>>();
        for (Entry<ContextChain, Collection<BindRule>> e: bindRules.entrySet()) {
            rules.put(e.getKey(), new ArrayList<BindRule>(e.getValue()));
        }
        return rules;
    }
    
    @SuppressWarnings("rawtypes")
    public InjectorConfiguration build() {
        // make a deep copy of the bind rules, since the map's key set can change
        // and the collection of bind rules can change
        final Map rules = getBindRulesDeepClone();
        
        return new InjectorConfiguration() {
            @Override
            @SuppressWarnings("unchecked")
            public Map<ContextChain, Collection<? extends BindRule>> getBindRules() {
                return Collections.unmodifiableMap(rules);
            }

            @Override
            public InjectSPI getSPI() {
                return spi;
            }
        };
    }
}
