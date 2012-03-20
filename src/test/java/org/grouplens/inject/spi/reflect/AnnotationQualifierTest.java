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
package org.grouplens.inject.spi.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;

import org.grouplens.inject.spi.reflect.types.ParameterA;
import org.grouplens.inject.spi.reflect.types.RoleA;
import org.grouplens.inject.spi.reflect.types.RoleB;
import org.grouplens.inject.spi.reflect.types.RoleC;
import org.grouplens.inject.spi.reflect.types.RoleD;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationQualifierTest {
    @Test
    public void testGetAnnotationType() throws Exception {
        AnnotationQualifier qualifier = new AnnotationQualifier(RoleA.class);
        Assert.assertEquals(RoleA.class, qualifier.getQualifierAnnotation());
    }
    
    @Test
    public void testEquals() throws Exception {
        AnnotationQualifier qualifier1 = new AnnotationQualifier(RoleA.class);
        AnnotationQualifier qualifier2 = new AnnotationQualifier(RoleA.class);
        AnnotationQualifier qualifier3 = new AnnotationQualifier(RoleB.class);
        
        Assert.assertEquals(qualifier1, qualifier2);
        Assert.assertFalse(qualifier1.equals(qualifier3));
    }
    
    @Test
    public void testParentRole() throws Exception {
        AnnotationQualifier qualifier = new AnnotationQualifier(RoleB.class);
        AnnotationQualifier parent = new AnnotationQualifier(RoleA.class);
        
        AnnotationQualifier dflt = new AnnotationQualifier(RoleD.class);
        
        Assert.assertEquals(parent, qualifier.getParentQualifier());
        Assert.assertNull(parent.getParentQualifier());
        Assert.assertFalse(parent.inheritsQualifier());
        
        Assert.assertNull(dflt.getParentQualifier());
        Assert.assertTrue(dflt.inheritsQualifier());
    }
    
    @Test
    public void testStaticIsRole() throws Exception {
        Assert.assertTrue(AnnotationQualifier.isQualifier(RoleA.class));
        Assert.assertTrue(AnnotationQualifier.isQualifier(ParameterA.class));
        Assert.assertFalse(AnnotationQualifier.isQualifier(Inherited.class));
    }
    
    @Test
    public void testStaticInheritsRole() throws Exception {
        doInheritsTest(RoleA.class, RoleA.class, true);
        doInheritsTest(RoleB.class, RoleA.class, true);
        doInheritsTest(RoleC.class, RoleA.class, true);
        doInheritsTest(RoleD.class, null, true);
        
        doInheritsTest(RoleA.class, RoleB.class, false);
        doInheritsTest(RoleD.class, RoleA.class, false);
        doInheritsTest(RoleA.class, null, false);
        doInheritsTest(null, RoleA.class, false);
        
        doInheritsTest(null, null, true);
    }
    
    private void doInheritsTest(Class<? extends Annotation> a, Class<? extends Annotation> b, boolean expected) {
        AnnotationQualifier ra = (a == null ? null : new AnnotationQualifier(a));
        AnnotationQualifier rb = (b == null ? null : new AnnotationQualifier(b));
        Assert.assertEquals(expected, AnnotationQualifier.inheritsQualifier(ra, rb));
    }
    
    @Test
    public void testStaticGetRoleDistance() throws Exception {
        doRoleDistanceTest(RoleA.class, RoleA.class, 0);
        doRoleDistanceTest(RoleB.class, RoleA.class, 1);
        doRoleDistanceTest(RoleC.class, RoleA.class, 2);
        doRoleDistanceTest(RoleD.class, null, 1);
        
        doRoleDistanceTest(RoleA.class, RoleB.class, -1);
        doRoleDistanceTest(RoleD.class, RoleA.class, -1);
        doRoleDistanceTest(RoleA.class, null, -1);
        doRoleDistanceTest(null, RoleA.class, -1);
        
        doRoleDistanceTest(null, null, 0);
    }
    
    private void doRoleDistanceTest(Class<? extends Annotation> a, Class<? extends Annotation> b, int expected) {
        AnnotationQualifier ra = (a == null ? null : new AnnotationQualifier(a));
        AnnotationQualifier rb = (b == null ? null : new AnnotationQualifier(b));
        Assert.assertEquals(expected, AnnotationQualifier.getQualifierDistance(ra, rb));
    }
}
