package org.grouplens.inject.graph;

public interface Injector {
    // FIXME: is the correct high-level interface, or should there even be one?
    // theoretically, we might not be operating on classes, etc.
    // but is that being too pedantic?
    public <T> T getInstance(Class<T> key);
    
    // What is the resolver responsible for doing?
    // 1. It is provided with a set of bind rules
    //    These bind rules are created/configured by the implementation of Nodes/Desires
    // 2. It is provided with a set of root desires that must be completed
    // 3. For each desire, search through all bind rules that satisfy the desire,
    //    these are sorted by the desire's ruleComparator() and the best match is selected.
    // 4. For #3, we also include the defaultBindRule() for the desire from the repository
    // 5. We must also include bind rules from higher contexts, but is this the responsibility
    //    of the resolver or the reflect impl. for managing contexts?
    //    - It is the resolvers, so we need some shared API for describing a context so
    //      that the bind rule creator can use them, and the resolver can manage them
    // 6. But once context-aware rules are in the list, they are sorted like before, although
    //    we may need to implement a wrapping rule that ensures proper depth selection
    // 7. We use the selected bind-rule (fail if none available), to apply to the desire
    //    to get a new desire. This new desire is used wherever the old desire was used so we
    //    get proper sharing -> although it would be nice if we could depend on equals() for desires and nodes
    // 8. We check if the new desire has a node. If it does, we include its dependency desires
    //    in the search, otherwise we repeat with the new desire and search for new matching
    //    bind rules.
    // 9. Eventually, we fail or we get to the state where all desires have been resolved to 
    //    a node, and their dependencies as well.  We then can instantiate all of them, although
    //    we'll need to do memoization or a topological sort to make sure we do it in the proper
    //    order.
    
    // FIXME: I need to review the decision on build container vs rec and session containers.
    // Is this part of this interface, or is that the responsibility of lenskit proper?
    
    // FIXME: I need to double check the instance instantiation requirements. Do we build all
    //  bind rules (no I don't think so), or just the ones matching the required roots (Yes?)
    // And after I've done the build instantiation, how do we mix those instances with the
    // session level instances?
    
    // What if I had a configurable parameter that said certain bind rules or desires had
    // to be session-level so it was totally type agnostic.  Then any desire or type that
    // was session level would push it (and the things depending on it) into the session container.
    
    // FIXME: I need to review the order preference for type distance, and context distance,
    //  I think as long as the type is satisfied, we go for deeper context match
}
