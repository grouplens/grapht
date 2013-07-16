package org.grouplens.grapht.solver;

import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.util.Preconditions;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A builder for {@link DependencySolver}s.
 *
 * @see org.grouplens.grapht.solver.DependencySolver#newBuilder()
 * @since 0.6
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DependencySolverBuilder {
    private List<BindingFunction> bindingFunctions = new LinkedList<BindingFunction>();
    private CachePolicy defaultPolicy = CachePolicy.NO_PREFERENCE;
    private int maxDepth = 100;

    /**
     * Get the current list of binding functions.
     * @return The current list of binding functions.
     */
    public List<BindingFunction> getBindingFunctions() {
        return Collections.unmodifiableList(bindingFunctions);
    }

    /**
     * Add a binding function to the dependency solver.
     * @param func The binding function.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunction(@Nonnull BindingFunction func) {
        Preconditions.notNull("binding function", func);
        bindingFunctions.add(func);
        return this;
    }

    /**
     * Add multiple binding function to the dependency solver.
     * @param funcs The binding functions.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunctions(@Nonnull BindingFunction... funcs) {
        for (BindingFunction fn: funcs) {
            addBindingFunction(fn);
        }
        return this;
    }

    /**
     * Add multiple binding function to the dependency solver.
     * @param funcs The binding functions.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunctions(@Nonnull Iterable<BindingFunction> funcs) {
        for (BindingFunction fn: funcs) {
            addBindingFunction(fn);
        }
        return this;
    }

    /**
     * Get the current default policy.
     * @return The builder's current default policy.
     */
    public CachePolicy getDefaultPolicy() {
        return defaultPolicy;
    }

    /**
     * Set the default policy for the solver.  This policy will be used when a satisfaction has a
     * policy of {@link CachePolicy#NO_PREFERENCE}.  The default is {@link CachePolicy#NO_PREFERENCE},
     * so the injector will make final caching decisions.
     * @param policy The default policy for the solver.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder setDefaultPolicy(CachePolicy policy) {
        defaultPolicy = policy;
        return this;
    }

    /**
     * Get the maximum depth.
     * @return The maximum object graph depth.
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Set the maximum object graph depth, for cycle detection.
     * @param depth The maximum object graph depth that the solver is allowed to produce.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder setMaxDepth(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("max depth must be at least 1");
        }
        maxDepth = depth;
        return this;
    }

    /**
     * Build a dependency solver.
     * @return The dependency solver.
     */
    public DependencySolver build() {
        return new DependencySolver(bindingFunctions, defaultPolicy, maxDepth);
    }
}
