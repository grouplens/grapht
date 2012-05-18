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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.ProviderSource;
import org.grouplens.grapht.util.Types;


/**
 * Satisfaction implementation wrapping an existing Provider instance. It has no
 * dependencies and it always returns the same Provider when
 * {@link #makeProvider(ProviderSource)} is invoked.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class ProviderInstanceSatisfaction extends ReflectionSatisfaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // "final"
    private Provider<?> provider;

    /**
     * Create a new satisfaction that wraps the given Provider instance.
     * 
     * @param provider The provider
     * @throws NullPointerException if provider is null
     */
    public ProviderInstanceSatisfaction(Provider<?> provider) {
        Checks.notNull("provider", provider);
        this.provider = provider;
    }
    
    /**
     * @return The provider instance returned by {@link #makeProvider(ProviderSource)}
     */
    public Provider<?> getProvider() {
        return provider;
    }
    
    @Override
    public List<? extends Desire> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Type getType() {
        return getErasedType();
    }

    @Override
    public Class<?> getErasedType() {
        return Types.getProvidedType(provider);
    }

    @Override
    public Provider<?> makeProvider(ProviderSource dependencies) {
        return provider;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ProviderInstanceSatisfaction)) {
            return false;
        }
        return ((ProviderInstanceSatisfaction) o).provider.equals(provider);
    }
    
    @Override
    public int hashCode() {
        return provider.hashCode();
    }
    
    @Override
    public String toString() {
        return "ProviderInstance(" + provider + ")";
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        provider = (Provider<?>) in.readObject();
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(provider);
    }
}
