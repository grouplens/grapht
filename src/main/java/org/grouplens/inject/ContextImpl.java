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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.inject.annotation.Parameter;
import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.ContextMatcher;

/**
 * ContextImpl is the basic implementation of Context.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class ContextImpl implements Context {
    private final ContextChain context;
    
    private final InjectorConfigurationBuilder config;
    
    public ContextImpl(InjectorConfigurationBuilder config, ContextChain context) {
        this.config = config;
        this.context = context;
    }
    
    /**
     * @return The root context
     */
    public InjectorConfigurationBuilder getBuilder() {
        return config;
    }
    
    /**
     * @return The context chain of this context
     */
    public ContextChain getContextChain() {
        return context;
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return new BindingImpl<T>(this, type);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void bind(Class<? extends Annotation> param, Object value) {
        Parameter p = param.getAnnotation(Parameter.class);
        if (p == null) {
            throw new IllegalArgumentException("Annotation must be annotated with Parameter");
        }
        Binding raw = bind(p.value()).withRole(param);
        raw.to(value);
    }

    @Override
    public Context in(Class<?> type) {
        return in(null, type);
    }

    @Override
    public Context in(@Nullable Class<? extends Annotation> role, Class<?> type) {
        ContextMatcher nextMatcher = config.getSPI().context(role, type);
        List<ContextMatcher> nextChain = new ArrayList<ContextMatcher>(context.getContexts());
        nextChain.add(nextMatcher);
        return new ContextImpl(config, new ContextChain(nextChain));
    }
}
