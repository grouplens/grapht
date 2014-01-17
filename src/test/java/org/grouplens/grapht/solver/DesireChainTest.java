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
package org.grouplens.grapht.solver;

import org.grouplens.grapht.reflect.Desire;
import org.grouplens.grapht.reflect.MockDesire;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DesireChainTest {
    @Test
    public void testSingleDesire() {
        Desire desire = new MockDesire(String.class, null, null);
        DesireChain chain = DesireChain.singleton(desire);
        assertThat(chain.getCurrentDesire(),
                   equalTo(desire));
        assertThat(chain.getInitialDesire(),
                   equalTo(desire));
        assertThat(chain, contains(desire));
        assertThat(chain.getPreviousDesires(), hasSize(0));
    }

    @Test
    public void testMultipleDesires() {
        Desire d1 = new MockDesire(InputStream.class, null, null);
        Desire d2 = new MockDesire(FileInputStream.class, null, null);
        DesireChain chain = DesireChain.singleton(d1).extend(d2);
        assertThat(chain.getCurrentDesire(),
                   equalTo(d2));
        assertThat(chain.getInitialDesire(),
                   equalTo(d1));
        assertThat(chain.getPreviousDesires(),
                   equalTo((List<Desire>) DesireChain.singleton(d1)));
        assertThat(chain, contains(d1, d2));
    }
}
