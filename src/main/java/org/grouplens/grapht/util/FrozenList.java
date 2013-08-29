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

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;

/**
 * An immutable list. This is useful for implementing immutable classes and cleanly
 * serializing them.
 * @deprecated Use {@link com.google.common.collect.ImmutableList} instead.
 */
@Deprecated
public class FrozenList<E> extends AbstractList<E> implements RandomAccess, Serializable {
    private static final long serialVersionUID = 1L;

    // the head node
    private final Node<E> head;
    // a cache for constant-time access
    private transient E[] cache;

    @SuppressWarnings("unchecked")
    public FrozenList(Collection<? extends E> elements) {
        cache = (E[]) elements.toArray();
        Node<E> cur = null;
        for (int i = cache.length - 1; i >= 0; i--) {
            cur = new Node(cache[i], cur);
        }
        head = cur;
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        try {
            stream.defaultReadObject();
        } catch (IOException e) {
            StreamCorruptedException ex = new StreamCorruptedException("I/O error");
            ex.initCause(e);
            throw ex;
        } catch (ClassNotFoundException e) {
            InvalidObjectException ex = new InvalidObjectException("Class not found");
            ex.initCause(e);
            throw ex;
        }

        // count the nodes
        int size = 0;
        for (Node<E> cur = head; cur != null; cur = cur.tail) {
            size += 1;
        }
        // build the cache
        cache = (E[]) new Object[size];
        int i = 0;
        for (Node<E> cur = head; cur != null; cur = cur.tail, i++) {
            cache[i] = cur.value;
        }
    }

    @Override
    public int size() {
        return cache.length;
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public E get(int idx) {
        return cache[idx];
    }

    @Nonnull @Override
    public Iterator<E> iterator() {
        return new IterImpl();
    }

    private class IterImpl implements Iterator<E> {
        private Node<E> next = head;

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            if (next == null) {
                throw new NoSuchElementException();
            } else {
                E val = next.value;
                next = next.tail;
                return val;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("cannot remove from frozen list");
        }
    }

    private static class Node<E> implements Serializable {
        private static final long serialVersionUID = 1L;

        private final E value;
        private final Node<E> tail;

        public Node(E val, Node<E> t) {
            value = val;
            tail = t;
        }
    }
}
