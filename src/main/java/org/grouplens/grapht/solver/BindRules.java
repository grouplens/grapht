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
