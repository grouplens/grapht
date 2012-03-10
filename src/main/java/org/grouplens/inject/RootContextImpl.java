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

class RootContextImpl implements Context {
    private final ContextImpl delegate;
    private final Set<Class<?>> defaultExcludes;
    
    private final Map<ContextChain, Collection<BindRule>> bindRules;
    
    public RootContextImpl(InjectSPI spi) {
        delegate = new ContextImpl(spi, this, new ContextChain(new ArrayList<ContextMatcher>()));
        defaultExcludes = new HashSet<Class<?>>();
        bindRules = new HashMap<ContextChain, Collection<BindRule>>();
    }
    
    public Map<ContextChain, Collection<BindRule>> getBindRules() {
        return bindRules;
    }
    
    public void addBindRule(ContextChain context, BindRule rule) {
        Collection<BindRule> inContext = bindRules.get(context);
        if (inContext == null) {
            inContext = new ArrayList<BindRule>();
            bindRules.put(context, inContext);
        }
        
        inContext.add(rule);
    }
    
    public void addDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.add(type);
    }

    public void removeDefaultExclusion(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Exclusion type cannot be null");
        }
        defaultExcludes.remove(type);
    }

    public Set<Class<?>> getDefaultExclusions() {
        return Collections.unmodifiableSet(defaultExcludes);
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
