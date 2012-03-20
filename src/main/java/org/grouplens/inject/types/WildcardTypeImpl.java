/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2012 Regents of the University of Minnesota and contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
