package org.grouplens.inject;

import java.util.Collection;
import java.util.Map;

import org.grouplens.inject.resolver.ContextChain;
import org.grouplens.inject.spi.BindRule;

public interface InjectorConfiguration {
    //  FIXME: I'm pretty sure I can simplify this, Ideally I'd like to 
    // have the InjectorConfiguration have the query given a List<Pair<Satisfaction, Role>>
    // for all matching desires, or something, but that might be too limiting
    public Map<ContextChain, Collection<? extends BindRule>> getBindRules();
}
