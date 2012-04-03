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
package org.grouplens.grapht.spi;

import java.lang.annotation.Annotation;

import javax.annotation.Nullable;

import org.grouplens.grapht.spi.Qualifier;

/**
 * MockQualifier is a simple Qualifier implementation that represents {@link Qualifier}s as unique
 * objects. It can map a hierarchy by referring to other parent {@link Qualifier}s.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockQualifier implements Qualifier {
    private final MockQualifier parent;
    private final boolean enableInheritence;
    
    public MockQualifier() {
        parent = null;
        enableInheritence = false;
    }
    
    public MockQualifier(@Nullable MockQualifier parent) {
        this.parent = parent;
        enableInheritence = true;
    }
    
    @Override
    public MockQualifier getParent() {
        return parent;
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
    public boolean inheritsDefault() {
        return parent != null && enableInheritence;
    }
}
