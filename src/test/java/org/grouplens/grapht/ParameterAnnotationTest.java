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
