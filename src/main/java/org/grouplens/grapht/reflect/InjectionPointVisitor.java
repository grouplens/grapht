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
/**
 * Interface for injection point visitors.  Instances of this interface are used to inspect or act on different types of injection points.
 *
 * @see InjectionPoint#accept(InjectionPointVisitor)
 */
package org.grouplens.grapht.reflect;


import org.grouplens.grapht.ConstructionException;
import org.grouplens.grapht.InjectionException;
import org.grouplens.grapht.Instantiator;
import org.grouplens.grapht.reflect.internal.*;

import java.util.Map;

/**
 * Interface for injection point visitors.
 * Instances of this interface are used to inspect or act on different types of injection points.
 * @see InjectionPoint#accept(InjectionPointVisitor)
 */
public interface InjectionPointVisitor {

    void visitField(FieldInjectionPoint ip) throws InjectionException;

    void visitSetter(SetterInjectionPoint ip) throws InjectionException;

    void visitNoArgument(NoArgumentInjectionPoint ip) throws InjectionException;

    void visitConstructor(ConstructorParameterInjectionPoint ip) throws InjectionException;

    void visitSynthetic(SimpleInjectionPoint ip) throws InjectionException;
}
