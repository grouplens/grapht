package org.grouplens.inject;

import org.grouplens.inject.spi.InjectSPI;
import org.grouplens.inject.spi.reflect.ReflectionInjectSPI;

public class Injector {
    private Injector() { }
    
    public static RootContext createContext() {
        return createContext(new ReflectionInjectSPI());
    }
    
    public static RootContext createContext(InjectSPI spi) {
        return new RootContextImpl(spi);
    }
}
