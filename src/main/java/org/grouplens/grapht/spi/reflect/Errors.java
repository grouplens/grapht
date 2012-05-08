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

import java.lang.reflect.Member;

import org.grouplens.grapht.ConfigurationException;
import org.grouplens.grapht.InjectionException;

/**
 * Utility to organize creation of common exceptions.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
final class Errors {
    private Errors() { }

    public static ConfigurationException notQualifier(Class<?> type) {
        return new ConfigurationException(type, "Annotation is not annotated with @Qualifier");
    }
    
    public static ConfigurationException notProvider(Class<?> type) {
        return new ConfigurationException(type, "Type does not implement Provider");
    }
    
    public static ConfigurationException invalidHierarchy(Class<?> source, Class<?> impl) {
        return new ConfigurationException(impl, "Type is not assignable to " + source);
    }
    
    public static ConfigurationException tooManyConstructors(Class<?> type) {
        throw new ConfigurationException(type, "More than one constructor with @Inject is not allowed");
    }
    
    public static InjectionException unexpectedNullValue(Member member) {
        return new InjectionException(member.getDeclaringClass(), member, 
                                      "Injection point is not annotated with @Nullable, but binding configuration provided a null value");
    }
    
    public static InjectionException notInstantiable(Class<?> type) {
        return new InjectionException(type, null, "No public default constructor or constructor annotated with @Inject");
    }
}
