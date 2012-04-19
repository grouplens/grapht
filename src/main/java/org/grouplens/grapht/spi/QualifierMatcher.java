package org.grouplens.grapht.spi;

/**
 * <p>
 * QualifierMatcher encapsulates the logic used to determine if a BindRule or
 * ContextMatcher match a particular Qualifier. Common qualifier matching rules
 * are:
 * <ol>
 * <li>Any qualifier</li>
 * <li>No qualifier</li>
 * <li>Annotation type</li>
 * <li>Annotation instance equality</li>
 * </ol>
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface QualifierMatcher {
    /**
     * Return true if this matcher matches the given Qualifier. It can be
     * assumed that the qualifier is not null and was created by the same SPI
     * that constructed this matcher.
     * 
     * @param q The qualifier to match
     * @return True if matched
     */
    boolean matches(Qualifier q);
}
