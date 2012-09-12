package org.grouplens.grapht.spi.reflect.types;

import org.grouplens.grapht.annotation.DefaultNull;

/**
 * @author Michael Ekstrand
 */
@DefaultNull
public class TypeDftN {
    private String string;

    public TypeDftN(String s) {
        string = s;
    }

    public String getString() {
        return string;
    }
}
