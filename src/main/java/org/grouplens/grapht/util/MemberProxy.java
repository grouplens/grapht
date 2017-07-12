/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
package org.grouplens.grapht.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Interface for proxies for class members.
 */
public interface MemberProxy extends Serializable {
    Member resolve() throws ClassNotFoundException, NoSuchMethodException;

    /**
     * Wrap a member in a proxy.
     * @param member The member to wrap (a constructor or a field).
     * @return The proxy object.
     */
    static MemberProxy of(Member member) {
        if (member instanceof Constructor) {
            return ConstructorProxy.of((Constructor) member);
        } else if (member instanceof Method) {
            return MethodProxy.of((Method) member);
        } else {
            throw new IllegalArgumentException("cannot wrap " + member + " in a proxy");
        }
    }
}
