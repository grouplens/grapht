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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Ordering is a utility Comparator implementation that creates compound
 * orderings from multiple Comparators.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 * @param <T> The compared type
 */
public class Ordering<T> implements Comparator<T> {
    private final List<Comparator<T>> comparators;
    
    private Ordering() {
        comparators = new ArrayList<Comparator<T>>();
    }
    
    /**
     * Create a new Ordering that wraps the given Comparator. The returned
     * ordering will compare objects equivalently to <tt>c</tt>.
     * 
     * @param c The base comparator
     * @return A new ordering wrapping the comparator
     * @throws NullPointerException if c is null
     */
    public static <T> Ordering<T> from(Comparator<T> c) {
        if (c == null) {
            throw new NullPointerException("Comparator cannot be null");
        }
        Ordering<T> o = new Ordering<T>();
        o.comparators.add(c);
        return o;
    }
    
    /**
     * Create a new Ordering that is the compound of this Ordering and the given
     * Comparator. The new Ordering will compare objects equivalently to this
     * Ordering, except that it uses <tt>c</tt> to determine equality in the
     * case where this Ordering returns 0.
     * 
     * @param c The Comparator to compound with
     * @return A new compounded ordering
     * @throws NullPointerException if c is null
     */
    public Ordering<T> compound(Comparator<T> c) {
        if (c == null) {
            throw new NullPointerException("Comparator cannot be null");
        }
        Ordering<T> no = new Ordering<T>();
        no.comparators.addAll(comparators);
        no.comparators.add(c);
        return no;
    }
    
    @Override
    public int compare(T o1, T o2) {
        for (Comparator<T> c: comparators) {
            int result = c.compare(o1, o2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}
