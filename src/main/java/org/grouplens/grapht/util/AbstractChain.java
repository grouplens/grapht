/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht.util;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.NotNull;

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
public abstract class AbstractChain<E extends Serializable> extends AbstractList<E> implements Serializable {
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

    public E getTailValue() {
        return tailValue;
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

    @NotNull
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
