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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.util.Preconditions;

import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A builder for {@link DependencySolver}s.
 *
 * @see org.grouplens.grapht.solver.DependencySolver#newBuilder()
 * @since 0.6
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class DependencySolverBuilder {
    private List<BindingFunction> bindingFunctions = new LinkedList<BindingFunction>();
    private List<BindingFunction> triggerFunctions = new LinkedList<BindingFunction>();
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
     * Get the current list of trigger binding functions.  These are the binding functions for which
     * {@code canTriggerRewrite} is {@code true} in {@link #addBindingFunction(BindingFunction,boolean)}.
     *
     * @return The current list of trigger binding functions.
     */
    public List<BindingFunction> getTriggerFunctions() {
        return Collections.unmodifiableList(triggerFunctions);
    }

    /**
     * Add a binding function to the dependency solver.
     * @param func The binding function.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunction(@NotNull BindingFunction func) {
        return addBindingFunction(func, true);
    }

    /**
     * Add a binding function to the dependency solver.
     * @param func The binding function.
     * @param canTriggerRewrite Controls whether this binding function can activate re-resolution
     *                          when doing a graph rewrite.  If {@code true} (the value passed by
     *                          all other binding function adders), this binding function returning
     *                          a binding will cause a graph rewrite; if {@code false}, it will be
     *                          ignored during rewrites until the rewrite has been triggered by some
     *                          other binding function.  Register the default binding function with
     *                          {@code false} here to keep default bindings from triggering rewrites.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunction(@NotNull BindingFunction func,
                                                      boolean canTriggerRewrite) {
        Preconditions.notNull("binding function", func);
        bindingFunctions.add(func);
        if (canTriggerRewrite) {
            triggerFunctions.add(func);
        }
        return this;
    }

    /**
     * Add multiple binding function to the dependency solver.
     * @param funcs The binding functions.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunctions(@NotNull BindingFunction... funcs) {
        return addBindingFunctions(Arrays.asList(funcs));
    }

    /**
     * Add multiple binding function to the dependency solver.
     * @param funcs The binding functions.
     * @return The builder (for chaining).
     */
    public DependencySolverBuilder addBindingFunctions(@NotNull Iterable<BindingFunction> funcs) {
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
        return new DependencySolver(bindingFunctions, triggerFunctions, defaultPolicy, maxDepth);
    }
}
