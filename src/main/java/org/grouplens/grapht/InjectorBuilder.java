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

import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.solver.BindingFunction;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DefaultInjector;
import org.grouplens.grapht.solver.ProviderBindingFunction;
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;

import java.lang.annotation.Annotation;

/**
 * <p>
 * InjectorBuilder is a Builder implementation that is capable of creating a
 * simple {@link Injector}. Additionally, it is root {@link Context} to make
 * configuring the built Injector as easy as possible. Injectors created by
 * InjectorBuilder instances memoize their created objects.
 * </p>
 * <p>
 * Internally, it uses an {@link BindingFunctionBuilder} to accumulate
 * bind rules, a {@link DefaultInjector} to resolve dependencies, and the
 * {@link ReflectionInjectSPI} to access dependency information.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectorBuilder extends AbstractContext {
    private final BindingFunctionBuilder builder;
    private CachePolicy cachePolicy;
    private boolean enableProviderInjection;

    /**
     * Create a new InjectorBuilder that automatically applies the given Modules
     * via {@link #applyModule(Module)}. Additional Modules can be applied later
     * as well. Configuration via the {@link Context} interface is also possible
     * (and recommended if Modules aren't used) before calling {@link #build()}.
     * 
     * @param modules Any modules to apply immediately
     */
    public InjectorBuilder(Module... modules) {
        builder = new BindingFunctionBuilder();
        for (Module m: modules) {
            applyModule(m);
        }
        cachePolicy = CachePolicy.MEMOIZE;
        enableProviderInjection = false;
    }
    
    /**
     * Set the default cache policy used by injectors created by this builder.
     * 
     * @param policy The default policy
     * @return This builder
     * @throws NullPointerException if policy is null
     * @throws IllegalArgumentException if policy is NO_PREFERENCE
     */
    public InjectorBuilder setDefaultCachePolicy(CachePolicy policy) {
        if (policy.equals(CachePolicy.NO_PREFERENCE)) {
            throw new IllegalArgumentException("Cannot be NO_PREFERENCE");
        }
        
        cachePolicy = policy;
        return this;
    }
    
    /**
     * Set whether or not to enable provider injection support in the built
     * Injectors.
     * 
     * @param enable True if the injector should support "provider injection"
     * @return This builder
     */
    public InjectorBuilder setProviderInjectionEnabled(boolean enable) {
        enableProviderInjection = enable;
        return this;
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return builder.getRootContext().bind(type);
    }
    
    @Override
    public <T> Binding<T> bindAny(Class<T> type) {
        return builder.getRootContext().bindAny(type);
    }
    
    @Override
    public Context within(Class<?> type) {
        return builder.getRootContext().within(type);
    }

    @Override
    public Context within(Class<? extends Annotation> qualifier, Class<?> type) {
        return builder.getRootContext().within(qualifier, type);
    }
    
    @Override
    public Context within(Annotation annot, Class<?> type) {
        return builder.getRootContext().within(annot, type);
    }

    @Override
    public Context at(Class<?> type) {
        return builder.getRootContext().at(type);
    }

    @Override
    public Context at(Class<? extends Annotation> qualifier, Class<?> type) {
        return builder.getRootContext().at(qualifier, type);
    }

    @Override
    public Context at(Annotation annot, Class<?> type) {
        return builder.getRootContext().at(annot, type);
    }

    /**
     * Apply a module to the root context of this InjectorBuilder (i.e.
     * {@link Module#configure(Context)}).
     * 
     * @param module The module to apply
     * @return This InjectorBuilder
     */
    public InjectorBuilder applyModule(Module module) {
        builder.applyModule(module);
        return this;
    }

    public Injector build() {
        BindingFunction[] functions;
        if (enableProviderInjection) {
            functions = new BindingFunction[] { 
                builder.build(RuleSet.EXPLICIT),
                builder.build(RuleSet.INTERMEDIATE_TYPES),
                builder.build(RuleSet.SUPER_TYPES),
                new ProviderBindingFunction(builder.getSPI()), // insert extra provider injection
                new DefaultDesireBindingFunction(builder.getSPI()) 
            };
        } else {
            functions = new BindingFunction[] { 
                builder.build(RuleSet.EXPLICIT),
                builder.build(RuleSet.INTERMEDIATE_TYPES),
                builder.build(RuleSet.SUPER_TYPES),
                new DefaultDesireBindingFunction(builder.getSPI()) 
            };
        }
        
        return new DefaultInjector(builder.getSPI(), cachePolicy, 100, functions);
    }
}
