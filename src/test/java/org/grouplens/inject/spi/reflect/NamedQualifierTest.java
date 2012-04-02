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
package org.grouplens.inject.spi.reflect;

import org.grouplens.inject.spi.Qualifier;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.junit.Assert;
import org.junit.Test;

public class NamedQualifierTest {
    @Test
    public void testGetName() throws Exception {
        NamedQualifier qualifier = new NamedQualifier("test");
        Assert.assertEquals("test", qualifier.getName());
    }
    
    @Test
    public void testEquals() throws Exception {
        NamedQualifier qualifier1 = new NamedQualifier("test1");
        NamedQualifier qualifier2 = new NamedQualifier("test1");
        NamedQualifier qualifier3 = new NamedQualifier("test2");
        
        Assert.assertEquals(qualifier1, qualifier2);
        Assert.assertFalse(qualifier1.equals(qualifier3));
    }
    
    @Test
    public void testParentRole() throws Exception {
        NamedQualifier qualifier = new NamedQualifier("test");
        Assert.assertNull(qualifier.getParent());
        Assert.assertFalse(qualifier.inheritsDefault());
    }
    
    @Test
    public void testStaticInheritsRole() throws Exception {
        NamedQualifier q1 = new NamedQualifier("test1");
        NamedQualifier q2 = new NamedQualifier("test1");
        NamedQualifier q3 = new NamedQualifier("test2");
        AnnotationQualifier q4 = new AnnotationQualifier(RoleA.class);
        
        // make sure @Named qualifiers equal to each other inherit each other
        doInheritsTest(q1, q2, true);
        doInheritsTest(q2, q1, true);
        
        // @Named qualifiers only inherit from qualifiers they're equal to
        doInheritsTest(q1, q3, false);
        
        // @Named qualifiers do not inherit from other annotation qualifiers
        doInheritsTest(q1, q4, false);
        doInheritsTest(q4, q1, false);
        
        // the default does not inherit, and is not inherited by @Named qualifiers
        doInheritsTest(null, q1, false);
        doInheritsTest(q1, null, false);
    }
    
    private void doInheritsTest(Qualifier a, Qualifier b, boolean expected) {
        Assert.assertEquals(expected, Qualifiers.inheritsQualifier(a, b));
    }
    
    @Test
    public void testStaticGetRoleDistance() throws Exception {
        NamedQualifier q1 = new NamedQualifier("test1");
        NamedQualifier q2 = new NamedQualifier("test1");
        NamedQualifier q3 = new NamedQualifier("test2");
        AnnotationQualifier q4 = new AnnotationQualifier(RoleA.class);
        
        // make sure @Named qualifiers equal to each other inherit each other
        doRoleDistanceTest(q1, q2, 0);
        doRoleDistanceTest(q2, q1, 0);
        
        // @Named qualifiers only inherit from qualifiers they're equal to
        doRoleDistanceTest(q1, q3, -1);
        
        // @Named qualifiers do not inherit from other annotation qualifiers
        doRoleDistanceTest(q1, q4, -1);
        doRoleDistanceTest(q4, q1, -1);
        
        // the default does not inherit, and is not inherited by @Named qualifiers
        doRoleDistanceTest(null, q1, -1);
        doRoleDistanceTest(q1, null, -1);
    }
    
    private void doRoleDistanceTest(Qualifier a, Qualifier b, int expected) {
        Assert.assertEquals(expected, Qualifiers.getQualifierDistance(a, b));
    }
}
