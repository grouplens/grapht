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
    
    // FIXME: we should add a Module-esque type so that create() can take
    // modules, and each module is provided with the root context to configure
    // bindings.  Then the bindings are processed and converted into a usable
    // injector.
}
