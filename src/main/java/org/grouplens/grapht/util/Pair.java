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
package org.grouplens.grapht.util;

/**
 * Pair represents a tuple of two items, possible of separate types.
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 *
 * @param <L> The type of the left or key item
 * @param <R> The type of the right or value item
 */
public class Pair<L, R> {
    private final L left;
    private final R right;
    
    /**
     * Create a new Pair composed of the two items.
     * 
     * @param left The left or key item
     * @param right The right or value item
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }
    
    /**
     * @return The left item of the pair, equivalent to {@link #getKey()}
     */
    public L getLeft() {
        return left;
    }
    
    /**
     * @return The key of the item pair, equivalent to {@link #getLeft()}
     */
    public L getKey() {
        return left;
    }
    
    /**
     * @return The right item of the pair, equivalent to {@link #getValue()}
     */
    public R getRight() {
        return right;
    }
    
    /**
     * @return The value of the item pair, equivalent to {@link #getRight()}
     */
    public R getValue() {
        return right;
    }
    
    /**
     * Convenience method to create a new pair composed of the two items. This
     * is the same as calling <code>new Pair<L, R>(left, right)</code>
     * 
     * @param left The left or key item
     * @param right The right or value item
     * @return A new pair containing left and right
     */
    public static <L, R> Pair<L, R> of(L left, R right) {
        return new Pair<L, R>(left, right);
    }
}
