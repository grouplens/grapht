package org.grouplens.inject.graph;

/**
 * A rule expressing an injector binding.  Bind rules can <i>match</i> desires,
 * in which case they apply to the desire.  They can then be <i>applied</i> to
 * the matching desire, returning a new desire which satisfies the original and
 * is (hopefully) one step closer to producing a fully-resolved type.
 * 
 * @author Michael Ekstrand <ekstrand@cs.umn.edu>
 *
 */
public interface BindRule {
    /**
     * Query whether this bind rule applies to some desire.
     * 
     * @param desire The desire to test.
     * @return <tt>true</tt> if this rule applies to the desire and can be used
     *         to further resolve it.
     */
    boolean matches(Desire desire);
    
    /**
     * Apply this rule to a desire, producing a desire <tt>d2</tt> such that
     * <code>desire.isSatisfiedBy(d2)</code> is true.
     * 
     * @param desire The desire to apply to.
     * @return The result of applying this rule to the desire, bringing it
     *         closer to resolution.
     */
    Desire apply(Desire desire);
}
