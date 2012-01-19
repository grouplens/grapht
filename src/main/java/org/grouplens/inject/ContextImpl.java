package org.grouplens.inject;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.ContextMatcher;
import org.grouplens.inject.spi.InjectSPI;

/**
 * ContextImpl is the basic implementation of Context.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
class ContextImpl implements Context {
    private final InjectSPI spi;
    private final ContextChain context;
    
    private final RootContextImpl root;
    
    public ContextImpl(InjectSPI spi, RootContextImpl root, ContextChain context) {
        this.spi = spi;
        this.root = root;
        this.context = context;
    }
    
    public RootContextImpl getRootContext() {
        return root;
    }
    
    public ContextChain getContextChain() {
        return context;
    }
    
    public InjectSPI getSPI() {
        return spi;
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return new BindingImpl<T>(this, type, (Class<?>[]) null);
    }

    @Override
    public <T> Binding<T> bind(Class<T> type, Class<?>... otherTypes) {
        return new BindingImpl<T>(this, type, otherTypes);
    }

    @Override
    public Context in(Class<?> type) {
        return in(null, type);
    }

    @Override
    public Context in(@Nullable Class<? extends Annotation> role, Class<?> type) {
        ContextMatcher nextMatcher = spi.context(role, type);
        List<ContextMatcher> nextChain = new ArrayList<ContextMatcher>(context.getContexts());
        nextChain.add(nextMatcher);
        return new ContextImpl(spi, root, new ContextChain(nextChain));
    }
}
