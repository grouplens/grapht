package org.grouplens.grapht.types.dft;

import javax.inject.Inject;

public class CDoubleDep {
    public final IDftImpl left;
    public final IDftImpl right;

    @Inject
    public CDoubleDep(IDftImpl a, IDftImpl b) {
        left = a;
        right = b;
    }
}
