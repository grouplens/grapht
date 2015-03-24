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

import org.grouplens.grapht.reflect.InjectionPoint;

import javax.annotation.Nullable;
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
