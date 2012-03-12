package org.grouplens.inject;

import java.util.Collection;
import java.util.Map;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.resolver.Resolver;
import org.grouplens.inject.spi.BindRule;

/**
 * InjectorConfiguration is a simple container for the accumulation of
 * {@link BindRule BindRules}. Generally, the {@link InjectorBuilder} or
 * {@link InjectorConfigurationBuilder} can be used without any other
 * modification. {@link Resolver Resolvers} may depend on an
 * InjectorConfiguration so they know what types are bound to which
 * dependencies.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface InjectorConfiguration {
    /**
     * Get all bind rules in this configuration. The bind rules are organized
     * first by the context that they were declared in. Each possible context
     * can have multiple bind rules within it. These bind rules can be of any
     * type, they are not restricted to being of the same type or role.
     * 
     * @return All bind rules, in an unmodifiable map
     */
    public Map<ContextChain, Collection<? extends BindRule>> getBindRules();
}
