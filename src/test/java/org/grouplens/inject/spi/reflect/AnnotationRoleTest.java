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

public class AnnotationRoleTest {
    @Test
    public void testGetAnnotationType() throws Exception {
        AnnotationRole role = new AnnotationRole(RoleA.class);
        Assert.assertEquals(RoleA.class, role.getRoleType());
    }
    
    @Test
    public void testIsParameter() throws Exception {
        AnnotationRole role1 = new AnnotationRole(ParameterA.class);
        AnnotationRole role2 = new AnnotationRole(RoleA.class);

        Assert.assertTrue(role1.isParameter());
        Assert.assertFalse(role2.isParameter());
    }
    
    @Test
    public void testEquals() throws Exception {
        AnnotationRole role1 = new AnnotationRole(RoleA.class);
        AnnotationRole role2 = new AnnotationRole(RoleA.class);
        AnnotationRole role3 = new AnnotationRole(RoleB.class);
        
        Assert.assertEquals(role1, role2);
        Assert.assertFalse(role1.equals(role3));
    }
    
    @Test
    public void testParentRole() throws Exception {
        AnnotationRole role = new AnnotationRole(RoleB.class);
        AnnotationRole parent = new AnnotationRole(RoleA.class);
        
        AnnotationRole dflt = new AnnotationRole(RoleD.class);
        
        Assert.assertEquals(parent, role.getParentRole());
        Assert.assertNull(parent.getParentRole());
        Assert.assertFalse(parent.inheritsRole());
        
        Assert.assertNull(dflt.getParentRole());
        Assert.assertTrue(dflt.inheritsRole());
    }
    
    @Test
    public void testStaticIsRole() throws Exception {
        Assert.assertTrue(AnnotationRole.isRole(RoleA.class));
        Assert.assertTrue(AnnotationRole.isRole(ParameterA.class));
        Assert.assertFalse(AnnotationRole.isRole(Inherited.class));
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
        AnnotationRole ra = (a == null ? null : new AnnotationRole(a));
        AnnotationRole rb = (b == null ? null : new AnnotationRole(b));
        Assert.assertEquals(expected, AnnotationRole.inheritsRole(ra, rb));
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
        AnnotationRole ra = (a == null ? null : new AnnotationRole(a));
        AnnotationRole rb = (b == null ? null : new AnnotationRole(b));
        Assert.assertEquals(expected, AnnotationRole.getRoleDistance(ra, rb));
    }
}
