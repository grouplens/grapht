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
