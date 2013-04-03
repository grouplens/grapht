package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.QualifierMatcher;
import org.grouplens.grapht.spi.Satisfaction;

/**
 * Utility methods for {@link BindRule}.
 */
public final class BindRules {
    private BindRules() {}

    public static BindRule toSatisfaction(Class<?> depType, Satisfaction satisfaction, CachePolicy policy,
                                          QualifierMatcher qualifier, boolean terminal) {
        return new BindRuleImpl(depType, satisfaction, policy, qualifier, terminal);
    }

    public static BindRule toClass(Class<?> depType, Class<?> implType, CachePolicy policy,
                                   QualifierMatcher qualifier, boolean terminal) {
        return new BindRuleImpl(depType, implType, policy, qualifier, terminal);
    }
}
