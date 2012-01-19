package org.grouplens.inject;

/**
 * CachePolicy controls the behavior of instant creation after dependency
 * resolution has been completed.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public enum CachePolicy {
    /**
     * A new instance is created every time a binding is used to satisfy a
     * dependency.
     */
    NEW,
    /**
     * <p>
     * Instances of a type are shared as much as possible. Because of
     * context-specific bindings, a type satisfying one dependency might require
     * a different set of resolved dependencies compared to another dependency
     * of the same type. In this case, separate instances are required.
     * <p>
     * However, when a type's resolved dependencies are the same, instances can
     * be shared. Thus, a type with no dependencies and a SHARED policy is
     * effectively a singleton within the scope of the configuration injector.
     */
    SHARED
}
