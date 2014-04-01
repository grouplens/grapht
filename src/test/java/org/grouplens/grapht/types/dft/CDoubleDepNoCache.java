package org.grouplens.grapht.types.dft;

import javax.inject.Inject;

public class CDoubleDepNoCache {
    public final IDftImplNoCache left;
    public final IDftImplNoCache right;

    @Inject
    public CDoubleDepNoCache(IDftImplNoCache a, IDftImplNoCache b) {
        left = a;
        right = b;
    }
}
