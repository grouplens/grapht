/*
 * LensKit, a reference implementation of recommender algorithms.
 * Copyright 2010-2011 Regents of the University of Minnesota
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
package org.grouplens.inject.reflect;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;

import javax.inject.Provider;

import org.grouplens.inject.spi.Desire;
import org.grouplens.inject.spi.Satisfaction;
import org.grouplens.inject.spi.reflect.ClasspathSatisfactionRepository;
import org.grouplens.inject.spi.reflect.InstanceSatisfaction;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Functions;

public class TestInstanceNode {
    ClasspathSatisfactionRepository repo;

    @Before
    public void initRepository() {
        repo = new ClasspathSatisfactionRepository();
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testSimpleClass() {
        Satisfaction nd = new InstanceSatisfaction("foobie bletch", String.class);
        assertThat(nd.getType(), equalTo((Type) String.class));
        assertThat(nd.getErasedType(), equalTo((Class) String.class));
        assertThat(nd.getDependencies(), Matchers.<Desire>empty());
    }

    @Test
    public void testProvider() {
        StringBuffer buf = new StringBuffer();
        Satisfaction nd = new InstanceSatisfaction(buf, StringBuffer.class);
        Provider<?> provider = nd.makeProvider(Functions.constant((Provider<?>) null));
        assertThat(provider, notNullValue());
        assertThat(provider.get(), sameInstance((Object) buf));
    }

    @Test
    public void testCreate() {
        File f = new File("foobie.bletch");
        Satisfaction nd = repo.newInstanceNode(f);
        assertThat(nd, notNullValue());
        assertThat(nd.makeProvider(Functions.constant((Provider<?>) null)).get(),
                sameInstance((Object) f));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateParameterized() {
        repo.newInstanceNode(new ArrayList<String>());
    }
}
