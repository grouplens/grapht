package org.grouplens.grapht.types.dft;

import javax.inject.Provider;

public class PDftProvider implements Provider<IDftProvider> {
    public static class Impl implements IDftProvider {
    }

    @Override
    public IDftProvider get() {
        return new Impl();
    }
}
