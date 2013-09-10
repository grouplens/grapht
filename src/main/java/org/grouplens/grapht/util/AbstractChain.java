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

import com.google.common.collect.Iterators;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for implementing chains, immutable reverse singly-linked lists.
 *
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractChain<E> extends AbstractList<E> implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final AbstractChain<E> previous;
    protected final E tailValue;
    protected final int length;

    /**
     * Construct a new chain node.
     * @param prev The previous node, or {@code null} for a singleton chain.
     * @param tv The value to go at the end of the chain.
     */
    protected AbstractChain(AbstractChain<E> prev, E tv) {
        previous = prev;
        tailValue = tv;
        if (prev == null) {
            length = 1;
        } else {
            length = prev.length + 1;
        }
    }

    @Override
    public int size() {
        return length;
    }

    @Override
    public E get(int i) {
        com.google.common.base.Preconditions.checkElementIndex(i, length);
        if (i == length - 1) {
            return tailValue;
        } else {
            assert previous != null;
            return previous.get(i);
        }
    }

    @Override
    public Iterator<E> iterator() {
        Iterator<E> current = Iterators.singletonIterator(tailValue);
        if (previous == null) {
            return current;
        } else {
            return Iterators.concat(previous.iterator(), current);
        }
    }

    /**
     * Iterate over this chain's elements in reverse order.
     * @return An iterator over the chain's elements in reverse order (current first).
     */
    public Iterator<E> reverseIterator() {
        return new Iterator<E>() {
            AbstractChain<E> cur = AbstractChain.this;
            @Override
            public boolean hasNext() {
                return cur != null;
            }

            @Override
            public E next() {
                if (cur == null) {
                    throw new NoSuchElementException();
                }
                E d = cur.tailValue;
                cur = cur.previous;
                return d;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public Iterable<E> reverse() {
        return new Iterable<E>() {
            @Override
            public Iterator<E> iterator() {
                return reverseIterator();
            }
        };
    }

    @Override
    public int hashCode() {
        // override hashCode so that lint tools don't complain
        return super.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof AbstractChain) {
            // optimize comparing two chains
            return Iterators.elementsEqual(reverseIterator(),
                                           ((AbstractChain) o).reverseIterator());
        } else {
            return super.equals(o);
        }
    }
}
