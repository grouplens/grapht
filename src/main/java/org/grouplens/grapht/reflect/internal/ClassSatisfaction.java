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

import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.reflect.*;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;


/**
 * ClassSatisfaction is a satisfaction that instantiates instances of a given
 * type.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class ClassSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = -1L;
    private final transient Class<?> type;

    /**
     * Create a satisfaction wrapping the given class type.
     * 
     * @param type The type to wrap
     * @throws NullPointerException if type is null
     * @throws IllegalArgumentException if the type cannot be instantiated
     */
    public ClassSatisfaction(Class<?> type) {
        Preconditions.notNull("type", type);

        this.type = Types.box(type);
        Preconditions.isInstantiable(this.type);
    }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }
    
    @Override
    public List<Desire> getDependencies() {
        return ReflectionDesire.getDesires(type);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Class<?> getErasedType() {
        return type;
    }

    @Override
    public boolean hasInstance() {
        return false;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitClass(type);
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Provider<?> makeProvider(Map<Desire,Provider<?>> dependencies) {
        return new InjectionProviderImpl(type, getDependencies(), dependencies);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ClassSatisfaction)) {
            return false;
        }
        return ((ClassSatisfaction) o).type.equals(type);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    @Override
    public String toString() {
        return "Class(" + type.getName() + ")";
    }

    private Object writeReplace() {
        return new SerialProxy(type);
    }

    private void readObject(ObjectInputStream stream) throws ObjectStreamException {
        throw new InvalidObjectException("must use serialization proxy");
    }

    private static class SerialProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final ClassProxy type;

        public SerialProxy(Class<?> cls) {
            type = ClassProxy.of(cls);
        }

        private Object readResolve() throws ObjectStreamException {
            try {
                return new ClassSatisfaction(type.resolve());
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("cannot resolve " + type);
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
