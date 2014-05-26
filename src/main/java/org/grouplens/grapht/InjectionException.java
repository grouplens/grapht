/*
 * Grapht, an open source dependency injector.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
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
 * Thrown when an Injector fails to instantiate a requested object. In many ways
 * this is similar to {@link java.lang.InstantiationException} except that it
 * exposes the type that could not be instantiated, and possible the member
 * (method, construcotr, or field) that could not be injected into.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class InjectionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Class<?> type;
    private final Member target;
    private final InjectionPoint injectionPoint;

    public InjectionException(InjectionPoint ip, String message) {
        this(ip, message, null);
    }

    public InjectionException(InjectionPoint ip, Throwable cause) {
        this(ip, null, cause);
    }

    public InjectionException(InjectionPoint ip, String message, Throwable cause) {
        super(message, cause);
        target = ip.getMember();
        type = target.getDeclaringClass();
        injectionPoint = ip;
    }

    public InjectionException(Class<?> type, @Nullable Member target) {
        this(type, target, "");
    }

    public InjectionException(Class<?> type, @Nullable Member target, String message) {
        this(type, target, message, null);
    }

    public InjectionException(Class<?> type, @Nullable Member target, Throwable cause) {
        this(type, target, "", cause);
    }

    public InjectionException(Class<?> type, @Nullable Member target, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
        this.target = target;
        injectionPoint = null;
    }

    /**
     * @return The Class type that could not be instantiated, or configured by
     *         injection
     */
    public Class<?> getType() {
        return type;
    }
    
    /**
     * @return The Member that is the target of injection, or null if the
     *         failure had no injection point associated with it
     */
    @Nullable
    public Member getTarget() {
        return target;
    }

    @Nullable
    public InjectionPoint getInjectionPoint() {
        return injectionPoint;
    }
    
    @Override
    public String getMessage() {
        if (injectionPoint != null) {
            return String.format("Error injecting into %s: %s", injectionPoint, super.getMessage());
        } else if (target != null) {
            return String.format("Error injecting into %s for %s: %s", target, type, super.getMessage());
        } else {
            return String.format("Error injecting for %s: %s", type, super.getMessage());
        }
    }
}
