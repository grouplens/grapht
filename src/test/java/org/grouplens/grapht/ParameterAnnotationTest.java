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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.grouplens.grapht.annotation.DefaultDouble;
import org.grouplens.grapht.annotation.DefaultInteger;
import org.grouplens.grapht.annotation.Parameter;
import org.grouplens.grapht.annotation.Parameter.PrimitiveType;
import org.junit.Assert;
import org.junit.Test;

public class ParameterAnnotationTest {
    @Test
    public void testBoxedIntBinding() {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(Integer.class).withQualifier(IntParameter.class).to(50);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testPrimitiveIntBinding() {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(int.class).withQualifier(IntParameter.class).to(50);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
    }
    
    @Test
    public void testBoxedDoubleBinding() {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(Double.class).withQualifier(DoubleParameter.class).to(50.0);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.0, t.b, 0.00001);
    }
    
    @Test
    public void testPrimitiveDoubleBinding() {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(double.class).withQualifier(DoubleParameter.class).to(50.0);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50.0, t.b, 0.00001);
    }
    
    @Test
    public void testDirectBinding() {
        InjectorBuilder b = new InjectorBuilder();
        b.bind(IntParameter.class, 50);
        b.bind(DoubleParameter.class, 50.0);
        Type t = b.build().getInstance(Type.class);
        
        Assert.assertEquals(50, t.a);
        Assert.assertEquals(50.0, t.b, 0.00001);
    }

    @Parameter(PrimitiveType.INT)
    @DefaultInteger(0)
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface IntParameter { }
    
    @Parameter(PrimitiveType.DOUBLE)
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
