package org.grouplens.inject.graph;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Provider;

import com.google.common.base.Function;

/**
 * A concrete type. It has a set of dependencies which must be satisfied in
 * order to instantiate it. It can also be viewed as an instantiable extension
 * of {@link Type}.
 * 
 * <p>
 * Nodes are expected to provide a reasonable implementation of
 * {@link #equals(Object)} and {@link #hashCode()} so that they can be
 * de-duplicated, etc.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 * 
 */
public interface Node {
    /**
     * Get this node's dependencies.
     * 
     * @return A list of dependencies which must be satisfied in order to
     *         instantiate this node.
     */
    List<Desire> getDependencies();
    
    /**
     * Get the type of this node.
     * 
     * @return The type of objects to be instantiated by this node.
     */
    Type getType();

    /**
     * Get the type-erased class of this node's type.
     * @return The class object for this node's type.
     */
    Class<?> getErasedType();
    
    /**
     * Create an instance of the type satisfied by this node.
     * 
     * @param dependencies A function mapping desires to providers of their
     *        instances.
     * @return A new instance of the type represented by this node, with the
     *         specified dependencies injected.
     * @review Consider supporting making Node create {@link Provider}s.
     */
    Object createInstance(Function<Desire, Provider<?>> dependencies);
}
