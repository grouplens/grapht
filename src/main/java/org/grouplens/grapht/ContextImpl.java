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

import org.grouplens.grapht.context.ContextElementMatcher;
import org.grouplens.grapht.context.ContextElements;
import org.grouplens.grapht.context.ContextPattern;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.context.Multiplicity;
import org.grouplens.grapht.reflect.Qualifiers;

import org.jetbrains.annotations.Nullable;
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
