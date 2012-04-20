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

import javax.annotation.Nullable;

class SimpleInjectionPoint implements InjectionPoint {
    private final AnnotationQualifier qualifier;
    private final Class<?> type;
    private final boolean nullable;
    
    public SimpleInjectionPoint(@Nullable AnnotationQualifier qualifier, Class<?> type, boolean nullable) {
        if (type == null) {
            throw new NullPointerException("Class type cannot be null");
        }
        this.qualifier = qualifier;
        this.type = type;
        this.nullable = nullable;
    }
    
    @Override
    public boolean isNullable() {
        return nullable;
    }
    
    @Override
    public Class<?> getType() {
        return type;
    }

    @Override
    public AnnotationQualifier getQualifier() {
        return qualifier;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() ^ (qualifier == null ? 0 : qualifier.hashCode());
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SimpleInjectionPoint)) {
            return false;
        }
        SimpleInjectionPoint p = (SimpleInjectionPoint) o;
        return p.type.equals(type) && (p.qualifier == null ? qualifier == null : p.qualifier.equals(qualifier)) && p.nullable == nullable;
    }
    
    @Override
    public String toString() {
        String q = (qualifier == null ? "" : qualifier + ":");
        return q + type.getSimpleName();
    }
}