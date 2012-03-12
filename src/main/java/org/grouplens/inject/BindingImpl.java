/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
import java.util.HashSet;
import java.util.Set;

import javax.inject.Provider;

import org.grouplens.inject.resolver.ContextChain;

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
    
    private Class<? extends Annotation> role;
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
    public Binding<T> withRole(Class<? extends Annotation> role) {
        if (role == null) {
            throw new NullPointerException("Role cannot be null");
        }
        validateState();
        this.role = role;
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
    public void to(Class<? extends T> impl) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        config.addBindRule(chain, config.getSPI().bindType(role, sourceType, impl, 0, terminate));
        // TODO create generated bindings based on source, impl,
        // and exclude sets
    }

    @Override
    public void to(T instance) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        config.addBindRule(chain, config.getSPI().bindInstance(role, sourceType, instance, 0));
    }

    @Override
    public void toProvider(Class<? extends Provider<? extends T>> provider) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        config.addBindRule(chain, config.getSPI().bindProvider(role, sourceType, provider, 0));
    }

    @Override
    public void toProvider(Provider<? extends T> provider) {
        ContextChain chain = context.getContextChain();
        InjectorConfigurationBuilder config = context.getBuilder();
        
        config.addBindRule(chain, config.getSPI().bindProvider(role, sourceType, provider, 0));
    }
}
