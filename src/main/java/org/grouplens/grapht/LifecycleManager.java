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

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Deque;
import java.util.LinkedList;

public class LifecycleManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(LifecycleManager.class);
    private Deque<TeardownAction> actions = new LinkedList<TeardownAction>();

    /**
     * Register a component with the lifecycle manager.  The component will be torn down when the lifecycle manager
     * is closed, using whatever teardown the lifecycle manager institutes.
     *
     * @param instance The component to register.
     */
    public void registerComponent(Object instance) {
        if (instance == null) {
            return;
        }

        if (instance instanceof AutoCloseable) {
            actions.add(new CloseAction((AutoCloseable) instance));
        }
        for (Method m: MethodUtils.getMethodsListWithAnnotation(instance.getClass(), PreDestroy.class)) {
            actions.add(new PreDestroyAction(instance, m));
        }
    }

    /**
     * Close the lifecycle manager, shutting down all components it manages.
     */
    @SuppressWarnings("squid:S1181") // catch Throwable - OK b/c we use it for ensuring cleanup
    @Override
    public void close() {
        Throwable error = null;
        while (!actions.isEmpty()) {
            TeardownAction action = actions.removeFirst();
            try {
                action.destroy();
            } catch (Throwable th) {
                if (error == null) {
                    error = th;
                } else {
                    error.addSuppressed(th);
                }
            }
        }
        if (error != null) {
            throw Throwables.propagate(error);
        }
    }

    /**
     * Interface for actions that tear down components.
     */
    interface TeardownAction {
        void destroy();
    }

    static class PreDestroyAction implements TeardownAction {
        private final Object instance;
        private final Method method;

        public PreDestroyAction(Object inst, Method m) {
            instance = inst;
            method = m;
        }

        @Override
        public void destroy() {
            try {
                logger.debug("invoking pre-destroy method {} on {}", method, instance);
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new UncheckedExecutionException("cannot access " + method, e);
            } catch (InvocationTargetException e) {
                throw new UncheckedExecutionException("error invoking " + method, e);
            }
        }
    }

    static class CloseAction implements TeardownAction {
        private final AutoCloseable instance;

        public CloseAction(AutoCloseable inst) {
            instance = inst;
        }

        @Override
        public void destroy() {
            try {
                logger.debug("closing {}", instance);
                instance.close();
            } catch (Exception e) {
                throw new UncheckedExecutionException("Error destroying " + instance, e);
            }
        }
    }
}
