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
package org.grouplens.grapht;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.inject.Qualifier;
import java.io.Closeable;
import java.lang.annotation.Annotation;

/**
 * <p>
 * Injector uses dependency injection to act as a factory for creating instances
 * with complex dependencies. A default implementation of Injector can easily be
 * created by using an {@link InjectorBuilder}:
 * 
 * <pre>
 * InjectorBuilder b = new InjectorBuilder();
 * b.bind(Foo.class).to(Bar.class);
 * b.applyModule(new MyCustomModule());
 * // other bindings
 * Injector i = b.build();
 * assert (i.getInstance(Foo.class) instanceof Bar);
 * </pre>
 * <p>
 * Alternatively, {@link BindingFunctionBuilder} and {@link org.grouplens.grapht.solver.DependencySolver}
 * can be used to create your own Injector implementations.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public interface Injector extends AutoCloseable {
    /**
     * Get an instance of T based on the bindings that this Injector was
     * configured with. An exception is thrown if the request type cannot be
     * instantiated with dependency injection.
     * <p>
     * Injectors may memoize or cache previously created objects. As an example,
     * the Injector created by {@link InjectorBuilder} reuses instances where
     * possible.
     * 
     * @param <T> The object type being created
     * @param type The class type
     * @return An instance of type T
     * @throws ConstructionException if type cannot be instantiated
     */
    @NotNull
    <T> T getInstance(Class<T> type) throws InjectionException;

    /**
     * Get an instance of T with the given {@link Qualifier} annotation.
     * 
     * @param <T> The object type
     * @param qualifier The qualifier on of the returned instance
     * @param type The class type
     * @return An instance of type T
     * @throws ConstructionException if type cannot be instantiated
     */
    @NotNull
    <T> T getInstance(Annotation qualifier, Class<T> type) throws InjectionException;

    /**
     * Try to get an instance of a component, returning {@code null} if the component
     * does not have a configured implementation.
     *
     * @param qualifier The qualifier, or {@code null} for an unqualified component.
     * @param type The component type.
     * @param <T> The component type.
     * @return An instance of type {@code T}, or {@code null} if no implemenation of {@code T} is configured.
     * @throws InjectionException if there is some other error injecting the instance.
     */
    @Nullable
    <T> T tryGetInstance(Annotation qualifier, Class<T> type) throws InjectionException;

    /**
     * Close the injector, shutting down any instantiated components that require shutdown.
     */
    void close();
}
