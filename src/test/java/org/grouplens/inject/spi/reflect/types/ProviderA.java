package org.grouplens.inject.spi.reflect.types;

import javax.inject.Provider;

public class ProviderA implements Provider<TypeA> {
    @Override
    public TypeA get() {
        return new TypeB();
    }
}
