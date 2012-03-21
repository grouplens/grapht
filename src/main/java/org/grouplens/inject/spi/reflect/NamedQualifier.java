/*
 * LensKit, an open source recommender systems toolkit.
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
package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;

import javax.inject.Named;

import org.grouplens.inject.spi.Qualifier;

/**
 * NamedQualifier is a Qualifier implementation that wraps String-based names
 * for use with the {@link Named} annotaiton.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class NamedQualifier implements Qualifier {
    private final String name;
    
    /**
     * Create a NamedQualifier that wraps the given String name.
     * 
     * @param name The name to match
     * @throws NullPointerException if name is null
     */
    public NamedQualifier(String name) {
        if (name == null) {
            throw new NullPointerException("Name cannot be null");
        }
        this.name = name;
    }
    
    /**
     * @return The matched name
     */
    public String getName() {
        return name;
    }
    
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        return new Annotation[0];
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return new Annotation[0];
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return false;
    }

    @Override
    public Qualifier getParent() {
        // NamedQualifier has no parent
        return null;
    }

    @Override
    public boolean inheritsDefault() {
        // NamedQualifier has no inheritence, not even the default
        return false;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NamedQualifier)) {
            return false;
        }
        return ((NamedQualifier) o).name.equals(name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    public String toString() {
        return "NamedQualifier(" + name + ")";
    }
}
