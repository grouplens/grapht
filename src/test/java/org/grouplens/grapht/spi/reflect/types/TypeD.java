package org.grouplens.grapht.spi.reflect.types;

import javax.inject.Inject;
import javax.inject.Provider;

public class TypeD {
    private final Provider<TypeC> provider;
    
    @Inject
    public TypeD(Provider<TypeC> provider) {
        this.provider = provider;
    }
    
    public Provider<TypeC> getProvider() {
        return provider;
    }
}
