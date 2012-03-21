package org.grouplens.inject.spi.reflect.types;

import javax.inject.Inject;
import javax.inject.Named;

public class NamedType {
    private final String n;
    
    @Inject
    public NamedType(@Named("test1") String t) {
        n = t;
    }
    
    public String getNamedString() {
        return n;
    }
}