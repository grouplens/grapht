package org.grouplens.inject.graph;

/**
 * A repository for obtaining type nodes and resolving desires.  The reflection
 * implementation uses annotations and subclassing relationships to attempt to
 * resolve desires from the classpath.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 */
public interface NodeRepository {
    /**
     * Look up the node for a desire, using whatever default lookup rules are
     * specified.
     * 
     * @param desire The desire to resolve.
     * @return The node resolved by this desire, or <tt>null</tt> if the desire
     *         is not instantiable and cannot be resolved.
     */
    Node resolve(Desire desire);
    
    /**
     * Get a bind rule which uses the repository's defaults to resolve desires.
     * 
     * @return A bind rule that uses defaults and annotations to resolve
     *         desires.
     */
    BindRule defaultBindRule();
}
