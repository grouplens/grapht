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
package org.grouplens.grapht.reflect.internal;

import org.grouplens.grapht.reflect.InjectionPoint;
import org.grouplens.grapht.util.MethodProxy;
import org.grouplens.grapht.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

public class NoArgumentInjectionPoint implements InjectionPoint, Serializable {
    private static final long serialVersionUID = -1L;
    private final transient Method method;

    /**
     * Create a NoArgumentInjectionPoint that wraps the given no-argument
     * method.
     * 
     * @param method The method to invoke without arguments
     */
    public NoArgumentInjectionPoint(@Nonnull Method method) {
        Preconditions.notNull("method", method);
        if (method.getParameterTypes().length != 0) {
            throw new IllegalArgumentException("Method takes arguments: " + method);
        }
        
        this.method = method;
    }
    
    /**
     * @return The setter method wrapped by this injection point
     */
    @Override @Nonnull
    public Method getMember() {
        return method;
    }
    
    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public Type getType() {
        return Void.class;
    }
    
    @Override
    public Class<?> getErasedType() {
        return Void.class;
    }

    @Nullable
    @Override
    public Annotation getQualifier() {
        return null;
    }

    @Nullable
    @Override
    public <A extends Annotation> A getAttribute(Class<A> atype) {
        return null;
    }

    @Override
    public Collection<Annotation> getAttributes() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NoArgumentInjectionPoint)) {
            return false;
        }
        NoArgumentInjectionPoint p = (NoArgumentInjectionPoint) o;
        return p.method.equals(method) && p.method == method;
    }
    
    @Override
    public int hashCode() {
        return method.hashCode();
    }
    
    @Override
    public String toString() {
        return method.getName() + "()";
    }

    private Object writeReplace() {
        return new SerialProxy(method);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;
        private final MethodProxy method;
        public SerialProxy(Method m) {
            method = MethodProxy.of(m);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new NoArgumentInjectionPoint(method.resolve());
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("no class for " + method.toString());
                ex.initCause(e);
                throw ex;
            } catch (NoSuchMethodException e) {
                InvalidObjectException ex =
                        new InvalidObjectException("cannot resolve " + method.toString());
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
