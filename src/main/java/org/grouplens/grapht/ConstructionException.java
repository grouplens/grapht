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

import org.grouplens.grapht.reflect.InjectionPoint;

import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Member;

/**
 * Thrown when there is an error constructing a component.  This is can be the result of an error
 * instantiating the object or a run-time incompatibility (e.g. a null dependency for a non-nullable
 * injection point; see {@link NullDependencyException}).
 *
 * @since 0.9
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ConstructionException extends InjectionException {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private final InjectionPoint injectionPoint;

    public ConstructionException(InjectionPoint ip, String message) {
        this(ip, message, null);
    }

    public ConstructionException(InjectionPoint ip, Throwable cause) {
        this(ip, defaultMessage(ip, null), cause);
    }

    public ConstructionException(InjectionPoint ip, String message, Throwable cause) {
        super(message, cause);
        type = null;
        injectionPoint = ip;
    }

    private static String defaultMessage(InjectionPoint ip, Class<?> type) {
        if (ip != null) {
            return String.format("Error injecting into %s", ip);
        } else {
            return String.format("Error injecting %s", type);
        }
    }

    public ConstructionException(String msg, Throwable cause) {
        super(msg, cause);
        type = null;
        injectionPoint = null;
    }

    public ConstructionException(Class<?> type, Throwable cause) {
        this(type, defaultMessage(null, type), cause);
    }

    public ConstructionException(Member target, String message, Throwable cause) {
        this(target.getDeclaringClass(), message, cause);
    }

    public ConstructionException(Class<?> type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        injectionPoint = null;
    }

    /**
     * @return The Class type that could not be instantiated, or configured by
     *         injection.
     */
    public Class<?> getType() {
        if (type != null) {
            return type;
        } else if (injectionPoint != null) {
            Member target = injectionPoint.getMember();
            if (target != null) {
                return target.getDeclaringClass();
            }
        }

        return null;
    }

    @Nullable
    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }
}
