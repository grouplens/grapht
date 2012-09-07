package org.grouplens.grapht.spi.reflect.types;

import javax.annotation.Nullable;
import javax.inject.Inject;

/**
 * Type that accepts null.
 */
public class TypeN {
    private InterfaceA object;

    @Inject
    public TypeN(@Nullable InterfaceA obj) {
        object = obj;
    }

    public InterfaceA getObject() {
        return object;
    }
}
