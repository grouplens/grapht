/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
