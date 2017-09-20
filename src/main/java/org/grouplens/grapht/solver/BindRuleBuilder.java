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
package org.grouplens.grapht.solver;

import com.google.common.base.Preconditions;
import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Qualifiers;
import org.grouplens.grapht.reflect.Satisfaction;

import java.util.EnumSet;

/**
 * Builder for bind rules.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class BindRuleBuilder {
    private Class<?> dependencyType;
    private QualifierMatcher qualifierMatcher = Qualifiers.matchAny();

    private Satisfaction satisfaction;
    private Class<?> implementation;

    private CachePolicy cachePolicy = CachePolicy.NO_PREFERENCE;
    private EnumSet<BindingFlag> flags = BindingFlag.emptySet();

    public static BindRuleBuilder create() {
        return new BindRuleBuilder();
    }

    /**
     * Get the dependency type to match.
     * @return The dependency type to match.
     */
    public Class<?> getDependencyType() {
        return dependencyType;
    }

    /**
     * Set the dependency type to match.
     * @param type The dependency type to match.
     */
    public BindRuleBuilder setDependencyType(Class<?> type) {
        dependencyType = type;
        return this;
    }

    /**
     * Get the configured qualifer matcher.  The initial qualifier matcher is {@link org.grouplens.grapht.reflect.Qualifiers#matchAny()}.
     * @return The qualifier matcher.
     */
    public QualifierMatcher getQualifierMatcher() {
        return qualifierMatcher;
    }

    /**
     * Set the qualifier matcher.
     * @param qm The qualifier matcher.
     */
    public BindRuleBuilder setQualifierMatcher(QualifierMatcher qm) {
        qualifierMatcher = qm;
        return this;
    }

    /**
     * Get the target satisfaction.
     * @return The configured satisfaction, or {@code null} if none is configured.
     */
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    /**
     * Set the satisfaction to bind to.  This will unset the implementation class and result in
     * a satisfaction binding.
     *
     * @param sat The satisfaction.
     */
    public BindRuleBuilder setSatisfaction(Satisfaction sat) {
        satisfaction = sat;
        return this;
    }

    /**
     * Get the target implementation.
     * @return The target implementation, or {@code null} if none is configured.
     */
    public Class<?> getImplementation() {
        return implementation;
    }

    /**
     * Set the target implementation. This will unset the satisfaction and result in an implementation
     * class binding.
     *
     * @param type The implementation class.
     */
    public BindRuleBuilder setImplementation(Class<?> type) {
        implementation = type;
        return this;
    }

    /**
     * Query whether the binding will be terminal.
     * @return {@code true} if the binding will be terminal.
     */
    public boolean isTerminal() {
        return flags.contains(BindingFlag.TERMINAL);
    }

    /**
     * Set whether the binding will be terminal.
     *
     * @param term {@code true} to create a terminal binding.
     * @see BindingFlag#TERMINAL
     */
    public BindRuleBuilder setTerminal(boolean term) {
        if (term) {
            flags.add(BindingFlag.TERMINAL);
        } else {
            flags.remove(BindingFlag.TERMINAL);
        }
        return this;
    }

    /**
     * Set the flags on this bind rule.
     * @param fs The flags.
     * @return The builder (for chaining).
     */
    public BindRuleBuilder setFlags(EnumSet<BindingFlag> fs) {
        flags = fs.clone();
        return this;
    }

    /**
     * Add a flag to the constructed bind rule.
     * @param flag The flag to add.
     * @return The builder (for chaining).
     */
    public BindRuleBuilder addFlag(BindingFlag flag) {
        flags.add(flag);
        return this;
    }

    /**
     * Get the cache policy.
     * @return The cache policy.
     */
    public CachePolicy getCachePolicy() {
        return cachePolicy;
    }

    /**
     * Set the cache policy.
     * @param policy The cache policy.
     */
    public BindRuleBuilder setCachePolicy(CachePolicy policy) {
        cachePolicy = policy;
        return this;
    }

    public BindRule build() {
        Preconditions.checkState(dependencyType != null, "no dependency type specified");
        if (implementation != null) {
            assert satisfaction == null;
            return new BindRuleImpl(dependencyType, implementation, cachePolicy, qualifierMatcher, flags);
        } else if (satisfaction != null) {
            return new BindRuleImpl(dependencyType, satisfaction, cachePolicy, qualifierMatcher, flags);
        } else {
            throw new IllegalStateException("no binding target specified");
        }
    }
}
