package org.grouplens.grapht.solver;

import com.google.common.collect.Iterators;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.grouplens.grapht.spi.Desire;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A sequence of desires.  When one desire is resolved, that resolution can be a desire that needs
 * further resolution.  These desires are accumulated in a desire chain.  Desire chains are
 * immutable; appending to one results in a new desire chain object pointing to the previous chain.
 * They form a reverse singly linked list.  The chain maintains O(1) access to both the initial and
 * current desires.
 *
 * <p>When iterating a desire chain the initial desire is first and the most recent desire is last.
 *
 * @since 0.7.0
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DesireChain implements Iterable<Desire> {
    @Nullable
    private final DesireChain previous;
    @Nonnull
    private final Desire desire;
    @Nonnull
    private final Desire initialDesire;

    public static DesireChain singleton(Desire desire) {
        return new DesireChain(null, desire);
    }

    /**
     * Create a new desire chain.
     * @param prev The previous chain.
     * @param d The desire.
     */
    private DesireChain(DesireChain prev, @Nonnull Desire d) {
        previous = prev;
        desire = d;
        initialDesire = prev == null ? d : prev.getInitialDesire();
    }

    @Nonnull
    public Desire getCurrentDesire() {
        return desire;
    }

    @Nonnull
    public Desire getInitialDesire() {
        return initialDesire;
    }

    /**
     * Extend this chain with a new desire. The chain is not modified; this method returns a new
     * chain that includes the new desire as its current desire.
     * 
     * @param d The new current desire.
     * @return The new desire chain.
     */
    @Nonnull
    public DesireChain extend(@Nonnull Desire d) {
        return new DesireChain(this, d);
    }

    @Override
    public Iterator<Desire> iterator() {
        Iterator<Desire> current = Iterators.singletonIterator(desire);
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
    Iterator<Desire> reverseIterator() {
        return new Iterator<Desire>() {
            DesireChain cur = DesireChain.this;
            @Override
            public boolean hasNext() {
                return cur != null;
            }

            @Override
            public Desire next() {
                if (cur == null) {
                    throw new NoSuchElementException();
                }
                Desire d = cur.desire;
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
    public int hashCode() {
        HashCodeBuilder hcb = new HashCodeBuilder();
        Iterator<Desire> iter = reverseIterator();
        while (iter.hasNext()) {
            hcb.append(iter.next());
        }
        return hcb.toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof DesireChain) {
            return Iterators.elementsEqual(reverseIterator(),
                                           ((DesireChain) o).reverseIterator());
        } else {
            return false;
        }
    }
}
