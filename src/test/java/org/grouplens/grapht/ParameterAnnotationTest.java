/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2017 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.grouplens.grapht;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.grapht.annotation.DefaultInteger;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.math.BigDecimal;
import java.math.BigInteger;

public class ParameterAnnotationTest {
    @Test
    public void testBoxedIntBinding() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        b.bind(Integer.class).withQualifier(IntParameter.class).to(50);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testPrimitiveIntBinding() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        b.bind(int.class).withQualifier(IntParameter.class).to(50);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testBoxedDoubleBinding() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        b.bind(Double.class).withQualifier(DoubleParameter.class).to(50.0);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.0, t.b, 0.00001);
    }
    
    @Test
    public void testPrimitiveDoubleBinding() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        b.bind(double.class).withQualifier(DoubleParameter.class).to(50.0);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.0, t.b, 0.00001);
    }

    private static void bindDouble(Context ctx, Object obj) {
        ctx.bind((Class) Double.class).withQualifier(DoubleParameter.class).to(obj);
    }
    private static void bindInt(Context ctx, Object obj) {
        ctx.bind((Class) Integer.class).withQualifier(IntParameter.class).to(obj);
    }
    
    @Test
    public void testDirectBinding() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindInt(b, 50);
        bindDouble(b, 50.0);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
        Assert.assertEquals(50.0, t.b, 0.00001);
    }
    
    @Test
    public void testFromBigIntegerCoercion() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindInt(b, BigInteger.valueOf(50));
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testFromBigDecimalCoercion() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindDouble(b, new BigDecimal(50.34));
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.34, t.b, 0.00001);
    }
    
    @Test
    public void testFromLongCoercion() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindInt(b, Long.valueOf(50));
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testFromFloatCoercion() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindDouble(b, Float.valueOf(50f));
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.0, t.b, 0.00001);
    }
    
    @Test
    public void testFromByteCoercion() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindInt(b, Byte.valueOf((byte) 50));
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testDiscreteToFloatCoercion() throws InjectionException {
        InjectorBuilder b = InjectorBuilder.create();
        bindDouble(b, Integer.valueOf(50));
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.0, t.b, 0.00001);
    }

    @DefaultInteger(0)
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface IntParameter { }
    
    @DefaultDouble(0.0)
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface DoubleParameter { }
    
    public static class Type {
        final int a;
        final double b;
        
        @Inject
        public Type(@IntParameter int a, 
                    @DoubleParameter double b) {
            this.a = a;
            this.b = b;
        }
    }
}
