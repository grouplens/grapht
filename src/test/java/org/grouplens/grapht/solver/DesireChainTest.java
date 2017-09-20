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
