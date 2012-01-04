/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.graph;

import java.util.Comparator;

/**
 * A possibly-not-concrete type. This represents the type of a dependency; it
 * may or may not be concrete. It can effectively be any type. Desires are
 * iteratively resolved and narrowed until they finally correspond to
 * {@link Satisfaction}s.
 *
 * <p>
 * There are two types of desires: parameter desires and component desires. They
 * are distinguished by the {@link #isParameter()} method. Parameter desires are
 * for primitive or string parameters; component desires are for objects and
 * have more complex resolution rules.
 *
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface Desire {
    /**
     * Query whether a particular satisfaction will satisfy this desire. If true, then
     * an instance of the type represented by <var>satisfaction</var> is type-compatible
     * with this desire. This is equivalent to
     * {@link Class#isAssignableFrom(Class)}.
     *
     * @review Do we actually need this method? I (ML) am inclined to delete
     *         it, since we'll likely be deleting isSatisfiedBy(Desire).
     *
     * @param satisfaction The satisfaction in question.
     * @return <tt>true</tt> if the satisfaction is compatible with the desire (can
     *         satisfy it); <tt>false</tt> otherwise.
     * @throws IllegalArgumentException if <var>satisfaction</var> is not from the same
     *         graph implementation as this desire.
     * @see Class#isAssignableFrom(Class)
     */
    boolean isSatisfiedBy(Satisfaction satisfaction);

    /**
     * Query whether a particular desire is a subset of this desire. If true,
     * then any node satisfying <var>desire</var> will also satisfy this desire.
     * This method is used to check establish transitive chains through desires
     * while resolving them to nodes.
     *
     * <p>
     * For parameter desires, this checks whether the other desire is for the
     * same type and parameter annotation, or a parameter inherited by this
     * desire's parameter annotation.
     *
     * <p>
     * For component desires, this checks whether the other desire is compatible
     * with this desire. Compatibility is defined by the following:
     * <ul>
     * <li>Anything satisfying the other desire is type-compatible with (can be
     * assigned to) this desire.
     * <li>Some as-yet-undefined type conversion rules.
     * </ul>
     *
     * @review Do we actually need this method? I (MDE) am inclined to delete
     *         it. {@link BindRule#matches(Desire)} may be all we need.
     * 
     * @param desire The desire to check.
     * @return <tt>true</tt> if any node satisfying <var>desire</var> will
     *         satisfy this desire.
     * @throws IllegalArgumentException if <var>desire</var> is not from the
     *         same graph implementation as this desire.
     * @see #isSatisfiedBy(Node)
     * @deprecated We don't think we need this method, so it's deprecated pending
     * removal.
     */
    @Deprecated
    boolean isSatisfiedBy(Desire desire);

    /**
     * Query whether this desire is for a parameter (primitive or string with a
     * parameter annotation).
     *
     * @deprecated Can this be removed and consumed by the Role interface?
     * @return <tt>true</tt> if this desire is for a parameter, <tt>false</tt>
     *         if it is for a component.
     */
    boolean isParameter();

    /**
     * Return the role that was declared with this desire. A desire without a
     * role should return null. No role can also be considered the default role.
     * 
     * @return The role applied to this desire
     */
    Role getRole();

    /**
     * Query whether this desire is instantiable, that is, resolved to a
     * concrete type. If it is instantiable, then it can be converted to a Satisfaction
     * with {@link #getSatisfaction()}.
     *
     * @return <tt>true</tt> if the desire is for a concrete class. The only
     *         further desires or satisfactions that can satisfy it are for subclasses
     *         of the desire type.
     */
    boolean isInstantiable();

    /**
     * Get the satisfaction (concrete type) if this desire is fully resolved.
     *
     * @return The satisfaction for this desire, or <tt>null</tt> if the desire is not a
     *         concrete type.
     */
    Satisfaction getSatisfaction();

    /**
     * Get a comparator for ordering bind rules.  The resulting comparator will
     * throw {@link IllegalArgumentException} when comparing bind rules from a
     * different implementation or which do not apply to this desire.
     *
     * @return A comparator that compares bind rules which apply to this desire
     *         in increasing order of closeness.
     */
    Comparator<BindRule> ruleComparator();
}
