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
package org.grouplens.grapht.spi.reflect;

import org.grouplens.grapht.spi.*;
import org.grouplens.grapht.util.ClassProxy;
import org.grouplens.grapht.util.InstanceProvider;
import org.grouplens.grapht.util.Preconditions;
import org.grouplens.grapht.util.Types;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * NullSatisfaction is a satisfaction that explicitly satisfies desires with the
 * <code>null</code> value.
 * 
 * @author <a href="http://grouplens.org">GroupLens Research</a>
 */
public class NullSatisfaction implements Satisfaction, Serializable {
    private static final long serialVersionUID = -1L;
    private final transient Class<?> type;
    
    /**
     * Create a NullSatisfaction that uses <code>null</code> to satisfy the
     * given class type.
     * 
     * @param type The type to satisfy
     * @throws NullPointerException if type is null
     */
    public NullSatisfaction(Class<?> type) {
        Preconditions.notNull("type", type);
        this.type = Types.box(type);
    }
    
    @Override
    public CachePolicy getDefaultCachePolicy() {
        return (getErasedType().getAnnotation(Singleton.class) != null ? CachePolicy.MEMOIZE : CachePolicy.NO_PREFERENCE);
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.emptyList();
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
        // Null satisfactions have instances, just null ones.
        return true;
    }

    @Override
    public <T> T visit(SatisfactionVisitor<T> visitor) {
        return visitor.visitNull();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Provider<?> makeProvider(ProviderSource dependencies) {
        return new InstanceProvider(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NullSatisfaction)) {
            return false;
        }
        return ((NullSatisfaction) o).type.equals(type);
    }
    
    @Override
    public int hashCode() {
        return type.hashCode();
    }
    
    @Override
    public String toString() {
        return "Null(" + type.getSimpleName() + ")";
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
                return new NullSatisfaction(type.resolve());
            } catch (ClassNotFoundException e) {
                InvalidObjectException ex = new InvalidObjectException("cannot resolve " + type);
                ex.initCause(e);
                throw ex;
            }
        }
    }
}
