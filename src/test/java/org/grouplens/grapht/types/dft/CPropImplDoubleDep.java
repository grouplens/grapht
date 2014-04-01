package org.grouplens.grapht.types.dft;

import javax.inject.Inject;

public class CPropImplDoubleDep {
    public final IPropDftImpl left;
    public final IPropDftImpl right;

    @Inject
    public CPropImplDoubleDep(IPropDftImpl a, IPropDftImpl b) {
        left = a;
        right = b;
    }
}
