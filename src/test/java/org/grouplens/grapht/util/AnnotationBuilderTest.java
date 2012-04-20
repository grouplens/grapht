package org.grouplens.grapht.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Assert;
import org.junit.Test;

public class AnnotationBuilderTest {
    @Test
    public void testArrayCloned() {
        // verify that array values are clones of the original
        // to prevent contamination, and that returned values are also
        // clones to prevent post-contamination
        
        double[] original = new double[] { 4.0 };
        A3 built = new AnnotationBuilder<A3>(A3.class).set("otherValue", original).build();
        double[] fromAnnot = built.otherValue();

        original[0] = 5.0;
        Assert.assertEquals(4.0, fromAnnot[0], 0.00001);
        
        fromAnnot[0] = 6.0;
        Assert.assertEquals(4.0, built.otherValue()[0], 0.00001);
    }
    
    @Test
    public void testDefaultValue() {
        // verify that a default value is returned when nothing was assigned to it
        A3 built = new AnnotationBuilder<A3>(A3.class).set("otherValue", new double[0]).build();
        
        Assert.assertEquals("hello", built.value());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testUnknownAttribute() {
        // verify that an attribute name not defined in the 
        // annotation fails
        new AnnotationBuilder<A3>(A3.class).set("notPresent", 4).build();
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testWrongSetter() {
        // verify that an attribute called with the wrong argument
        // type fails
        new AnnotationBuilder<A2>(A2.class).set("value", 6.0).build();
    }
    
    @Test(expected=IllegalStateException.class)
    public void testNoValueAssignedForRequiredAttribute() {
        // verify that not assigning a required fails at build time
        new AnnotationBuilder<A2>(A2.class).build();
    }
    
    @Test
    public void testSetValues() {
        // verify that all setters set the expected values
        Types t = new AnnotationBuilder<Types>(Types.class)
            .set("v1", (byte) 1)
            .set("v2", (short) 2)
            .set("v3", 3)
            .set("v4", 4L)
            .set("v5", '5')
            .set("v6", 6f)
            .set("v7", 7.0)
            .set("v8", "8")
            .set("v9", new byte[] { 9 })
            .set("v10", new short[] { 10 })
            .set("v11", new int[] { 11 })
            .set("v12", new long[] { 12 })
            .set("v13", new char[] { 't' })
            .set("v14", new float[] { 14f })
            .set("v15", new double[] { 15.0 })
            .set("v16", new String[] { "16" })
            .set("v17", new AnnotationBuilder<A2>(A2.class).set("value", 17).build())
            .set("v18", new A2[] { new AnnotationBuilder<A2>(A2.class).set("value", 18).build() })
            .set("v19", true)
            .set("v20", new boolean[] { true, false })
            .build();
        
        Assert.assertEquals((byte) 1, t.v1());
        Assert.assertEquals((short) 2, t.v2());
        Assert.assertEquals(3, t.v3());
        Assert.assertEquals(4L, t.v4());
        Assert.assertEquals('5', t.v5());
        Assert.assertEquals(6f, t.v6(), 0.00001f);
        Assert.assertEquals(7.0, t.v7(), 0.00001);
        Assert.assertEquals("8", t.v8());
        
        Assert.assertEquals(1, t.v9().length);
        Assert.assertEquals((byte) 9, t.v9()[0]);
        
        Assert.assertEquals(1, t.v10().length);
        Assert.assertEquals((short) 10, t.v10()[0]);
        
        Assert.assertEquals(1, t.v11().length);
        Assert.assertEquals(11, t.v11()[0]);
        
        Assert.assertEquals(1, t.v12().length);
        Assert.assertEquals(12L, t.v12()[0]);
        
        Assert.assertEquals(1, t.v13().length);
        Assert.assertEquals('t', t.v13()[0]);
        
        Assert.assertEquals(1, t.v14().length);
        Assert.assertEquals(14f, t.v14()[0], 0.00001f);
        
        Assert.assertEquals(1, t.v15().length);
        Assert.assertEquals(15.0, t.v15()[0], 0.00001);
        
        Assert.assertEquals(1, t.v16().length);
        Assert.assertEquals("16", t.v16()[0]);
        
        Assert.assertEquals(17, t.v17().value());
        
        Assert.assertEquals(1, t.v18().length);
        Assert.assertEquals(18, t.v18()[0].value());
        
        Assert.assertTrue(t.v19());
        
        Assert.assertEquals(2, t.v20().length);
        Assert.assertTrue(t.v20()[0]);
        Assert.assertFalse(t.v20()[1]);
    }
    
    @Test
    public void testNoAttributes() {
        // verify that a no attribute annotation implements
        // the annotation type, hashCode, and equals properly
        A1 built = new AnnotationBuilder<A1>(A1.class).build();
        
        Assert.assertEquals(A1.class, built.annotationType());
        Assert.assertTrue(built.equals(jvmA1a1));
        Assert.assertTrue(jvmA1a1.equals(built));
        Assert.assertEquals(jvmA1a1.hashCode(), built.hashCode());
    }
    
    @Test
    public void testSingleAttribute() {
        // verify that a single attribute correctly implements
        // the annotation type, hashCode, and equals properly
        A2 equal = new AnnotationBuilder<A2>(A2.class).set("value", 4).build();
        A2 notEqual = new AnnotationBuilder<A2>(A2.class).set("value", 0).build();

        Assert.assertEquals(A2.class, equal.annotationType());
        Assert.assertEquals(A2.class, notEqual.annotationType());
        Assert.assertTrue(equal.equals(jvmA2a1));
        Assert.assertTrue(jvmA2a1.equals(equal));
        Assert.assertEquals(jvmA2a1.hashCode(), equal.hashCode());
        
        Assert.assertFalse(notEqual.equals(jvmA2a1));
        Assert.assertFalse(jvmA2a1.equals(notEqual));
        Assert.assertFalse(jvmA2a1.hashCode() == notEqual.hashCode());
    }
    
    @Test
    public void testMultipleAttribute() {
        // verify that multiple attributes correctly implements
        // the annotation type, hashCode, and equals properly
        A3 equal = new AnnotationBuilder<A3>(A3.class).set("value", "world")
                                                      .set("otherValue", new double[] { 1.0, 2.0 })
                                                      .build();
        A3 notEqual = new AnnotationBuilder<A3>(A3.class).set("value", "hoopla")
                                                         .set("otherValue", new double[] { 1.0, 2.0 })
                                                         .build();

        Assert.assertEquals(A3.class, equal.annotationType());
        Assert.assertEquals(A3.class, notEqual.annotationType());
        Assert.assertTrue(equal.equals(jvmA3a1));
        Assert.assertTrue(jvmA3a1.equals(equal));
        Assert.assertEquals(jvmA3a1.hashCode(), equal.hashCode());
        
        Assert.assertFalse(notEqual.equals(jvmA3a1));
        Assert.assertFalse(jvmA3a1.equals(notEqual));
        Assert.assertFalse(jvmA3a1.hashCode() == notEqual.hashCode());
    }
    
    @Test
    public void testAnnotationArrayAttribute() {
        // verify that an annotation with annotation arrays implements
        // the annotation type, hashCode, and equals properly
        A4 equal = new AnnotationBuilder<A4>(A4.class).set("arrays", new A3[] {
            new AnnotationBuilder<A3>(A3.class).set("otherValue", new double[] { 0.5 }).build(),
            new AnnotationBuilder<A3>(A3.class).set("otherValue", new double[] { 0.0 })
                                               .set("value", "george").build()
        }).build();
        A4 notEqual = new AnnotationBuilder<A4>(A4.class).set("arrays", new A3[] {
            new AnnotationBuilder<A3>(A3.class).set("otherValue", new double[] { 0.4, 0.2 }).build(),
            new AnnotationBuilder<A3>(A3.class).set("otherValue", new double[] { 0.0, 2.3 })
                                               .set("value", "bob").build()
        }).build();

        Assert.assertEquals(A4.class, equal.annotationType());
        Assert.assertEquals(A4.class, notEqual.annotationType());
        Assert.assertTrue(equal.equals(jvmA4a1));
        Assert.assertTrue(jvmA4a1.equals(equal));
        Assert.assertEquals(jvmA4a1.hashCode(), equal.hashCode());
        
        Assert.assertFalse(notEqual.equals(jvmA4a1));
        Assert.assertFalse(jvmA4a1.equals(notEqual));
        Assert.assertFalse(jvmA4a1.hashCode() == notEqual.hashCode());
    }
    
    private static final A1 jvmA1a1;
    private static final A2 jvmA2a1;
    private static final A3 jvmA3a1;
    private static final A4 jvmA4a1;
    
    static {
        
        try {
            jvmA1a1 = AP.class.getMethod("a1").getAnnotation(A1.class);
            jvmA2a1 = AP.class.getMethod("a1").getAnnotation(A2.class);
            jvmA3a1 = AP.class.getMethod("a1").getAnnotation(A3.class);
            jvmA4a1 = AP.class.getMethod("a1").getAnnotation(A4.class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static class AP {
        @A1 
        @A2(4)
        @A3(otherValue={ 1.0, 2.0 }, value="world")
        @A4(arrays={ @A3(otherValue=0.5), @A3(otherValue=0.0, value="george") })
        public void a1() { }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public static @interface A1 { }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface A2 {
        int value();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface A3 {
        double[] otherValue();
        
        String value() default "hello";
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface A4 {
        A3[] arrays();
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Types {
        byte v1();
        short v2();
        int v3();
        long v4();
        char v5();
        float v6();
        double v7();
        String v8();
        byte[] v9();
        short[] v10();
        int[] v11();
        long[] v12();
        char[] v13();
        float[] v14();
        double[] v15();
        String[] v16();
        A2 v17();
        A2[] v18();
        boolean v19();
        boolean[] v20();
    }
}
