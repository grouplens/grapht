package org.grouplens.grapht.spi.reflect.types;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Type that accepts null.
 */
public class TypeN2 {
    private InterfaceA object;

    @Inject
    public TypeN2(InterfaceA obj) {
        object = obj;
    }

    public InterfaceA getObject() {
        return object;
    }
}
