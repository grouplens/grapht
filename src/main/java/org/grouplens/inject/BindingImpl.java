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
import org.grouplens.inject.spi.InjectSPI;

class BindingImpl<T> implements Binding<T> {
    private final ContextImpl context;
    private final Set<Class<?>> sourceTypes;
    
    private final Set<Class<?>> excludeTypes;
    
    private Class<? extends Annotation> role;
    private boolean terminate;
    
    private boolean bindingCompleted;
    
    public BindingImpl(ContextImpl context, Class<T> type, Class<?>... otherTypes) {
        this.context = context;
        sourceTypes = new HashSet<Class<?>>();
        excludeTypes = new HashSet<Class<?>>();
        
        sourceTypes.add(type);
        if (otherTypes != null) {
            for (Class<?> t: otherTypes) {
                sourceTypes.add(t);
            }
        }
        
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
    public Binding<T> terminateChain() {
        validateState();
        terminate = true;
        return this;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void to(Class<? extends T> impl) {
        InjectSPI spi = context.getSPI();
        ContextChain chain = context.getContextChain();
        RootContextImpl root = context.getRootContext();
        
        for (Class<?> source: sourceTypes) {
            root.addBindRule(chain, spi.bindType(role, (Class) source, impl, 0, terminate));
        }
        // TODO create generated bindings based on source, impl,
        // and exclude sets
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void to(T instance) {
        InjectSPI spi = context.getSPI();
        ContextChain chain = context.getContextChain();
        RootContextImpl root = context.getRootContext();
        
        for (Class<?> source: sourceTypes) {
            root.addBindRule(chain, spi.bindInstance(role, (Class) source, instance, 0, terminate));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void toProvider(Class<? extends Provider<? extends T>> provider) {
        InjectSPI spi = context.getSPI();
        ContextChain chain = context.getContextChain();
        RootContextImpl root = context.getRootContext();
        
        for (Class<?> source: sourceTypes) {
            root.addBindRule(chain, spi.bindProvider(role, (Class) source, provider, 0, terminate));
        }
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void toProvider(Provider<? extends T> provider) {
        InjectSPI spi = context.getSPI();
        ContextChain chain = context.getContextChain();
        RootContextImpl root = context.getRootContext();
        
        for (Class<?> source: sourceTypes) {
            root.addBindRule(chain, spi.bindProvider(role, (Class) source, provider, 0, terminate));
        }        
    }
}
