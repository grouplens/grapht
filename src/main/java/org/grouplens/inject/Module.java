package org.grouplens.inject;

/**
 * Module represents a grouping of related configuration. An entire project
 * could be configured in a single Module, or multiple components can be
 * separated. This also allows you to share example configurations if your
 * application has complex possibilities. An example of this is configuring an
 * item-item recommender.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public interface Module {
    /**
     * Configure bindings given the root Context, <tt>ct</tt>.
     * 
     * @param ctx The root context
     */
    public void bind(Context ctx);
}
