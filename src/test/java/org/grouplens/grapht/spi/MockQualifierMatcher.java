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
package org.grouplens.grapht.spi;

import java.util.HashSet;
import java.util.Set;

public class MockQualifierMatcher implements QualifierMatcher {
    private Set<MockQualifier> qualifiers;
    
    private MockQualifierMatcher() {
        qualifiers = null;
    }
    
    public static MockQualifierMatcher any() {
        return new MockQualifierMatcher();
    }
    
    public static MockQualifierMatcher none() {
        return match((MockQualifier) null);
    }
    
    public static MockQualifierMatcher match(MockQualifier... mockQualifiers) {
        MockQualifierMatcher q = new MockQualifierMatcher();
        q.qualifiers = new HashSet<MockQualifier>();
        for (MockQualifier m: mockQualifiers) {
            q.qualifiers.add(m);
        }
        return q;
    }
    
    @Override
    public int compareTo(QualifierMatcher o) {
        MockQualifierMatcher mq = (MockQualifierMatcher) o;
        
        if (qualifiers == null) {
            if (mq.qualifiers == null) {
                return 0;
            } else {
                return 1;
            }
        } else {
            if (mq.qualifiers == null) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public boolean matches(Qualifier q) {
        return qualifiers == null || qualifiers.contains(q);
    }
}
