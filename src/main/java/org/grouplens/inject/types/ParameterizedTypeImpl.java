/*
 * LensKit, an open source recommender systems toolkit.
 * Copyright 2010-2011 Regents of the University of Minnesota
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

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * Implementation of parameterized types.
 * @author Michael Ekstrand
 */
class ParameterizedTypeImpl implements ParameterizedType {
    final private Class<?> rawClass;
    final private Type[] actuals;
    final private Type ownerType;

    public ParameterizedTypeImpl(Class<?> cls, Type[] types, Type owner) {
        rawClass = cls;
        actuals = types;
        ownerType = owner;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actuals;
    }
    
    @Override
    public Class<?> getRawType() {
        return rawClass;
    }
    
    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) o;
            if (!rawClass.equals(pt.getRawType())) {
                return false;
            } else if (!Arrays.equals(actuals, pt.getActualTypeArguments())) {
                return false;
            } else {
                // they're the same (including null), or they're equal.
                return ownerType == pt.getOwnerType() ||
                        (ownerType != null && ownerType.equals(pt.getOwnerType()));
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int base = rawClass.hashCode() ^ Arrays.hashCode(actuals);
        if (ownerType == null) {
            return base;
        } else {
            return base ^ ownerType.hashCode();
        }
    }
    
    public String toString() {
        return String.format("%s<%s>", rawClass, StringUtils.join(actuals, ", "));
    }
}
