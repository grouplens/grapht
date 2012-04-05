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

public class Ordering<T> implements Comparator<T> {
    private final List<Comparator<T>> comparators;
    
    private Ordering() {
        comparators = new ArrayList<Comparator<T>>();
    }
    
    public static <T> Ordering<T> from(Comparator<T> c) {
        Ordering<T> o = new Ordering<T>();
        o.comparators.add(c);
        return o;
    }
    
    public Ordering<T> compound(Comparator<T> c) {
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
