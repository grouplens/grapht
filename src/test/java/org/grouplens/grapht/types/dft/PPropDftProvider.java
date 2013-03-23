package org.grouplens.grapht.types.dft;

import javax.inject.Provider;

public class PPropDftProvider implements Provider<IPropDftProvider> {
    public static class Impl implements IPropDftProvider {
    }

    @Override
    public IPropDftProvider get() {
        return new Impl();
    }
}
