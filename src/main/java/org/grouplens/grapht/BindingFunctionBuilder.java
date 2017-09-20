/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.grouplens.grapht.context.ContextMatcher;
import org.grouplens.grapht.solver.BindRule;
import org.grouplens.grapht.solver.BindingFunction;
import org.grouplens.grapht.solver.RuleBasedBindingFunction;

import java.io.Externalizable;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * BindingFunctionBuilder provides a convenient access to the fluent API and
 * converts calls to {@link Context} and {@link Binding} methods into multiple
 * {@link BindingFunction BindingFunctions}.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
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
    
    private final Context root;
    
    private final Set<Class<?>> defaultExcludes;
    private final boolean generateRules;
    
    private final Multimap<ContextMatcher,BindRule> manualRules;
    private final Multimap<ContextMatcher,BindRule> intermediateRules; // "generated"
    private final Multimap<ContextMatcher,BindRule> superRules; // "generated"

    /**
     * Create a new InjectorConfigurationBuilder that automatically generates bind rules for
     * super and intermediate types.
     */
    public BindingFunctionBuilder() {
        this(true);
    }

    /**
     * Create a new InjectorConfigurationBuilder. If <tt>generateRules</tt> is true, bind
     * rules for super and intermediate types are generated. If it is false,
     * only one bind rule is created per binding.
     * 
     * @param generateRules True if additional bind rules should be generated
     */
    public BindingFunctionBuilder(boolean generateRules) {
        this.generateRules = generateRules;

        defaultExcludes = new HashSet<Class<?>>();
        defaultExcludes.add(Object.class);
        defaultExcludes.add(Comparable.class);
        defaultExcludes.add(Serializable.class);
        defaultExcludes.add(Externalizable.class);
        defaultExcludes.add(Cloneable.class);

        manualRules = ArrayListMultimap.create();
        intermediateRules = ArrayListMultimap.create();
        superRules = ArrayListMultimap.create();

        root = ContextImpl.root(this);
    }
    
    private BindingFunctionBuilder(BindingFunctionBuilder clone) {
        generateRules = clone.generateRules;
        defaultExcludes = new HashSet<Class<?>>(clone.defaultExcludes);
        manualRules = ArrayListMultimap.create(clone.manualRules);
        intermediateRules = ArrayListMultimap.create(clone.intermediateRules);
        superRules = ArrayListMultimap.create(clone.superRules);
        root = ContextImpl.root(this);
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
     * @return The root context managed by this builder
     */
    public Context getRootContext() {
        return root;
    }

    /**
     * Run the module's {@link Module#configure(Context) bind()} method on the root
     * context of this builder.
     * 
     * @param module The module to apply
     */
    public void applyModule(Module module) {
        module.configure(getRootContext());
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
    public BindingFunction build(RuleSet set) {
        return new RuleBasedBindingFunction(getMap(set));
    }
    
    void addBindRule(RuleSet set, ContextMatcher context, BindRule rule) {
        Multimap<ContextMatcher, BindRule> map = getMap(set);
        map.put(context, rule);
    }

    Set<Class<?>> getDefaultExclusions() {
        return Collections.unmodifiableSet(defaultExcludes);
    }

    private Multimap<ContextMatcher, BindRule> getMap(RuleSet set) {
        switch(set) {
        case EXPLICIT:
            return manualRules;
        case INTERMEDIATE_TYPES:
            return intermediateRules;
        case SUPER_TYPES:
            return superRules;
        default:
            throw new IllegalArgumentException("Invalid rule set " + set);
        }
    }
}
