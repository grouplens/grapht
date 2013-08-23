package org.grouplens.grapht.util;

import com.google.common.collect.Iterators;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Base class for implementing chains, immutable reverse singly-linked lists.
 *
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public abstract class AbstractChain<E> extends AbstractList<E> {
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
    protected Iterator<E> reverseIterator() {
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
