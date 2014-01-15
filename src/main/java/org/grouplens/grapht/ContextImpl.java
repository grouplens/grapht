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

import org.grouplens.grapht.spi.context.ContextElementMatcher;
import org.grouplens.grapht.spi.context.ContextElements;
import org.grouplens.grapht.spi.context.ContextPattern;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.context.Multiplicity;
import org.grouplens.grapht.spi.reflect.Qualifiers;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * ContextImpl is the basic implementation of Context.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
class ContextImpl extends AbstractContext {
    private final BindingFunctionBuilder config;
    private final ContextPattern pattern;
    private final boolean anchored;

    /**
     * Construct a new context implementation.
     * @param config The function builder.
     * @param context The context pattern.
     * @param anchored Whether this is anchored.  If it is not anchored, {@code .*} will be appended
     *                 to the context pattern for any calls other than to {@link #matching(ContextPattern)}.
     */
    private ContextImpl(BindingFunctionBuilder config, ContextPattern context, boolean anchored) {
        this.config = config;
        this.pattern = context;
        this.anchored = anchored;
    }

    public static ContextImpl root(BindingFunctionBuilder config) {
        return new ContextImpl(config, ContextPattern.empty(), false);
    }
    
    public BindingFunctionBuilder getBuilder() {
        return config;
    }
    
    /**
     * Get the context pattern for this builder.  It will never return {@code null}; for the root
     * context, it will return {@link ContextPattern#any()}.
     *
     * @return The context pattern for this builder.
     */
    public ContextPattern getContextPattern() {
        if (anchored) {
            return pattern;
        } else {
            return pattern.appendDotStar();
        }
    }
    
    @Override
    public <T> Binding<T> bind(Class<T> type) {
        return new BindingImpl<T>(this, type);
    }

    @Override
    public Context within(Class<?> type) {
        return in(Qualifiers.matchDefault(), type, false);
    }

    @Override
    public Context within(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return in(Qualifiers.match(qualifier), type, false);
    }
    
    @Override
    public Context within(@Nullable Annotation annot, Class<?> type) {
        return in(Qualifiers.match(annot), type, false);
    }

    @Override
    public Context matching(ContextPattern pat) {
        return new ContextImpl(config, pattern.append(pat), true);
    }

    @Override
    public Context at(Class<?> type) {
        return in(Qualifiers.matchDefault(), type, true);
    }

    @Override
    public Context at(@Nullable Class<? extends Annotation> qualifier, Class<?> type) {
        return in(Qualifiers.match(qualifier), type, true);
    }

    @Override
    public Context at(@Nullable Annotation annot, Class<?> type) {
        return in(Qualifiers.match(annot), type, true);
    }
    
    private Context in(QualifierMatcher q, Class<?> type, boolean anchored) {
        ContextElementMatcher nextMatcher = ContextElements.matchType(type, q);
        ContextPattern pat = getContextPattern().append(nextMatcher, Multiplicity.ONE);

        return new ContextImpl(config, pat, anchored);
    }
}
