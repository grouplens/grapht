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

import javax.inject.Provider;

/**
 * A class with several inner classes implementing Provider for testing
 * purposes.
 */
public class TestingProviders {

    public static class Target implements Cloneable{}
    public static class GenericTarget<T>{}

    /*
     * The simplest form of provider. This has no generics or anything else sophisticated.
     */
    public static class SimpleProvider implements Provider<Object>{
        public Target get(){
            return null;
        }
    }
    /*****************************************************************************/

    /*
     * Creates the situation that TypedProviders are most needed.
     *
     * The Provider is parametrized over a type variable which is subject to type erasure
     */
    public static abstract class AbstractProvider<T> implements Provider<T>{
        public T get(){
            return null;
        }
    }
    public static abstract class AbstractProvider2<T> extends AbstractProvider<T>{}

    public static class SubtypedProvider extends AbstractProvider<Target>{}

    public static class SubtypedProvider2 extends AbstractProvider2<Target>{}

   /*****************************************************************************/

   /*
    * Same as the above, instead of abstract classes, provider is found up the interface hierarchy.
    */
   public static interface InterfaceProvider<T> extends Provider<T>{}

   public static interface InterfaceProvider2<T> extends InterfaceProvider<T>{}

    public static class ImplementedProvider implements InterfaceProvider<Target>{
        public Target get(){
            return null;
        }
    }

    public static class ImplementedProvider2 implements InterfaceProvider2<Target>{
        public Target get(){
            return null;
        }
    }

    /*****************************************************************************/

    /*
     * These are Providers with unbound type variables. These are **not** supported.
     */
    public static class BoundedProvider<T extends Target> implements Provider<T>{
        public T get(){
            return null;
        }
    }


    public static class UnboundedProvider<T> implements Provider<T>{
        public T get(){
            return null;
        }
    }

    public static class MultiBoundProvider<T extends Target & Cloneable> implements Provider<T>{
        public T get(){
            return null;
        }
    }
}
