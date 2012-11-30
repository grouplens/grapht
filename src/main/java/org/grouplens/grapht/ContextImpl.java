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

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.grapht.spi.ElementChainContextMatcher;
import org.grouplens.grapht.spi.ContextElementMatcher;
import org.grouplens.grapht.spi.QualifierMatcher;

/**
 * ContextImpl is the basic implementation of Context.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class ContextImpl extends AbstractContext {
    private final ElementChainContextMatcher context;
    
    private final BindingFunctionBuilder config;
    
    public ContextImpl(BindingFunctionBuilder config, ElementChainContextMatcher context) {
        this.config = config;
        this.context = context;
    }
    
    public BindingFunctionBuilder getBuilder() {
        return config;
    }
    
    /**
     * @return The context chain of this context
     */
    public ElementChainContextMatcher getContextChain() {
        return context;
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return new BindingImpl<T>(this, type);
    }

    @Override
    public Context in(Class<?> type) {
        return in(config.getSPI().matchAny(), type);
    }

    @Override
    public Context in(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return in(config.getSPI().match(qualifier), type);
    }
    
    @Override
    public Context in(@Nullable Annotation annot, Class<?> type) {
        return in(config.getSPI().match(annot), type);
    }
    
    private Context in(QualifierMatcher q, Class<?> type) {
        ContextElementMatcher nextMatcher = config.getSPI().context(q, type);
        
        List<ContextElementMatcher> nextChain = new ArrayList<ContextElementMatcher>(context.getContexts());
        nextChain.add(nextMatcher);
        return new ContextImpl(config, new ElementChainContextMatcher(nextChain));
    }
}
