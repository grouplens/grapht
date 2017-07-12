/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
                throw new RuntimeException("cannot access " + method, e);
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
