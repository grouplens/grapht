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

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.reflect.QualifierMatcher;
import org.grouplens.grapht.reflect.Satisfaction;

/**
 * Utility methods for {@link BindRule}.
 */
public final class BindRules {
    private BindRules() {}

    /**
     * Construct a new bind rule that binds a dependency to a satisfaction.
     *
     *
     * @param depType      The dependency type.
     * @param qualifier    The qualifier matcher to match the dependency type.
     * @param satisfaction The satisfaction.
     * @param policy       The cache policy.
     * @param terminal     Whether this binding is terminal.
     * @return A new bind rule.
     * @deprecated Use {@link BindRuleBuilder}.
     */
    @Deprecated
    public static BindRule toSatisfaction(Class<?> depType, QualifierMatcher qualifier,
                                          Satisfaction satisfaction, CachePolicy policy,
                                          boolean terminal) {
        return BindRuleBuilder.create()
                .setDependencyType(depType)
                .setQualifierMatcher(qualifier)
                .setSatisfaction(satisfaction)
                .setCachePolicy(policy)
                .setTerminal(terminal)
                .build();
    }

    /**
     * Construct a new bind rule that binds a dependency to a class.
     *
     *
     * @param depType   The dependency type.
     * @param qualifier The qualifier matcher to match the dependency type.
     * @param implType  The implementation type to use.
     * @param policy    The cache policy.
     * @param terminal  Whether this binding is terminal.
     * @return A new bind rule.
     * @deprecated Use {@link BindRuleBuilder}.
     */
    @Deprecated
    public static BindRule toClass(Class<?> depType, QualifierMatcher qualifier,
                                   Class<?> implType, CachePolicy policy,
                                   boolean terminal) {
        return BindRuleBuilder.create()
                              .setDependencyType(depType)
                              .setQualifierMatcher(qualifier)
                              .setImplementation(implType)
                              .setCachePolicy(policy)
                              .setTerminal(terminal)
                              .build();
    }
}
