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
package org.grouplens.inject.spi;

import java.util.Comparator;

/**
 * MockDesire is a simple Desire implementation for use within certain types of
 * tests.
 * 
 * @author Michael Ludwig <mludwig@cs.umn.edu>
 */
public class MockDesire implements Desire {
    private final Qualifier qualifier;
    private final Satisfaction satisfaction;
    
    private final Desire defaultDesire;
    
    public MockDesire() {
        this(null);
    }
    
    public MockDesire(Satisfaction satisfaction) {
        this(satisfaction, null);
    }
    
    public MockDesire(Satisfaction satisfaction, Qualifier qualifier) {
        this(satisfaction, qualifier, null);
    }
    
    public MockDesire(Satisfaction satisfaction, Qualifier qualifier, Desire dflt) {
        this.satisfaction = satisfaction;
        this.qualifier = qualifier;
        this.defaultDesire = dflt;
    }
    
    @Override
    public boolean isInstantiable() {
        return satisfaction != null;
    }

    @Override
    public Satisfaction getSatisfaction() {
        return satisfaction;
    }

    @Override
    public Comparator<BindRule> ruleComparator() {
        return new Comparator<BindRule>() {
            @Override
            public int compare(BindRule o1, BindRule o2) {
                return 0;
            }
        };
    }

    @Override
    public Qualifier getQualifier() {
        return qualifier;
    }

    @Override
    public Desire getDefaultDesire() {
        return defaultDesire;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
