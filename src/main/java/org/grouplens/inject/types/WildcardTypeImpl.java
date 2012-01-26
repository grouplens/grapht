package org.grouplens.inject.types;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * @author Michael Ekstrand
 */
class WildcardTypeImpl implements WildcardType {
    private final @Nonnull Type[] upperBounds;
    private final @Nonnull Type[] lowerBounds;

    public WildcardTypeImpl(@Nonnull Type[] upper, @Nonnull Type[] lower) {
        upperBounds = upper;
        lowerBounds = lower;
    }

    @Override
    public Type[] getLowerBounds() {
        return lowerBounds;
    }

    @Override
    public Type[] getUpperBounds() {
        return upperBounds;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof WildcardType) {
            WildcardType w = (WildcardType) o;
            return Arrays.equals(upperBounds, w.getUpperBounds()) && Arrays.equals(lowerBounds, w.getLowerBounds());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(upperBounds) ^ Arrays.hashCode(lowerBounds);
    }
    
    @Override
    public String toString() {
        StringBuilder bld = new StringBuilder();
        bld.append("?");
        for (Type t: upperBounds) {
            bld.append(" extends ");
            bld.append(t.toString());
        }
        for (Type t: lowerBounds) {
            bld.append(" super ");
            bld.append(t.toString());
        }
        return bld.toString();
    }
}
