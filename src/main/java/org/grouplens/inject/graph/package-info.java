/**
 * Interface to the type graph API used by the configuration injector. This API
 * provides means to compare and resolve types, get their dependencies, and
 * otherwise navigate the classpath in order to resolve dependencies. Types are
 * represented as a graph of nodes with edges to their dependencies.
 * 
 * <p>
 * Important to the graph API is the notion of a <i>concrete type</i>. Concrete
 * types are defined inductively:
 * <ul>
 * <li>Any non-parameterized, non-abstract class is a concrete type.
 * <li>Any parameterized non-abstract class all of whose parameters are
 * bound to concrete types is a concrete type.
 * </ul>
 */
package org.grouplens.inject.graph;
