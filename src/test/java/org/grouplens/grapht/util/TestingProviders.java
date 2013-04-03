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
    public static class SimpleProvider implements Provider<Target>{
        public Target get(){
            return null;
        }
    }
    /*****************************************************************************/

    public static abstract class AbstractProvider<T> implements Provider<T>{
        public T get(){
            return null;
        }
    }
    public static abstract class AbstractProvider2<T> extends AbstractProvider<T>{}

    public static class SubtypedProvider extends AbstractProvider<Target>{}

    public static class SubtypedProvider2 extends AbstractProvider2<Target>{}

   /*****************************************************************************/

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

    /*****************************************************************************/

    public static class GenericProvider<T> implements Provider<GenericTarget<T>>{
        public GenericTarget<T> get(){
            return null;
        }
    }
}
