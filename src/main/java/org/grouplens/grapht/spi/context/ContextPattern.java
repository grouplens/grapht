package org.grouplens.grapht.spi.context;

import org.grouplens.grapht.util.AbstractChain;

import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * A regular pattern matching contexts.
 *
 * @since 0.7
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class ContextPattern implements Serializable {
    private static final long serialVersionUID = 1L;

    @Nullable
    private final Chain tokenChain;

    public ContextPattern() {
        tokenChain = null;
    }

    private ContextPattern(Chain tokens) {
        tokenChain = tokens;
    }

    /**
     * Append an element to this pattern.  The pattern is not modified; rather, a new pattern
     * extended with the new matching element is returned.
     * @param match The element matcher.
     * @param mult The multiplicity of this element.
     * @return This pattern extended by the new element.
     */
    ContextPattern append(ContextElementMatcher match, Multiplicity mult) {
        Element elem = new Element(match, mult);
        Chain chain = tokenChain == null ? new Chain(elem) : tokenChain.extend(elem);
        return new ContextPattern(chain);
    }

    /**
     * An element of the context pattern.
     */
    public static class Element implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ContextElementMatcher matcher;
        private final Multiplicity multiplicity;

        public Element(ContextElementMatcher mat, Multiplicity mult) {
            matcher = mat;
            multiplicity = mult;
        }

        public ContextElementMatcher getMatcher() {
            return matcher;
        }

        public Multiplicity getMultiplicity() {
            return multiplicity;
        }
    }

    /**
     * A chain of context pattern elements.
     */
    private static class Chain extends AbstractChain<Element> {
        private static final long serialVersionUID = 1L;

        public Chain(Element val) {
            super(null, val);
        }

        private Chain(Chain head, Element val) {
            super(head, val);
        }

        public Chain extend(Element elem) {
            return new Chain(this, elem);
        }

        public Chain getLeading() {
            return (Chain) previous;
        }
    }
}
