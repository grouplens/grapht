/*
 * Grapht, an open source dependency injector.
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
package org.grouplens.grapht.spi.context;

/**
 * Multiplicity of element matches - how many times may/must an element match?
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public enum Multiplicity {
    /**
     * Match exactly once.
     */
    ONE {
        @Override
        public boolean isOptional() {
            return false;
        }

        @Override
        public boolean isConsumed() {
            return true;
        }
    },
    /**
     * Match zero or more times.
     */
    ZERO_OR_MORE {
        @Override
        public boolean isOptional() {
            return true;
        }

        @Override
        public boolean isConsumed() {
            return false;
        }
    };

    public abstract boolean isOptional();
    public abstract boolean isConsumed();
}
