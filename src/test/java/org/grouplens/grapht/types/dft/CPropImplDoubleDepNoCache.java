package org.grouplens.grapht.types.dft;

import javax.inject.Inject;

public class CPropImplDoubleDepNoCache {
    public final IPropDftImplNoCache left;
    public final IPropDftImplNoCache right;

    @Inject
    public CPropImplDoubleDepNoCache(IPropDftImplNoCache a, IPropDftImplNoCache b) {
        left = a;
        right = b;
    }
}
