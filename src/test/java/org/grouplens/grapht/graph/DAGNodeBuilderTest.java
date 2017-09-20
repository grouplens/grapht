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
package org.grouplens.grapht.graph;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class DAGNodeBuilderTest {
    @Test
    public void testGetLabel() {
        DAGNodeBuilder<String,String> bld = DAGNode.newBuilder();
        assertThat(bld.getLabel(), nullValue());
        bld.setLabel("foo");
        assertThat(bld.getLabel(), equalTo("foo"));
    }

    @Test
    public void testGetEdges() {
        DAGNodeBuilder<String,String> bld = DAGNode.newBuilder("foo");
        assertThat(bld.getEdges(), hasSize(0));
        DAGNode<String,String> nbr = DAGNode.singleton("bar");
        bld.addEdge(nbr, "piper");
        assertThat(bld.getEdges(),
                   contains(Pair.of(nbr, "piper")));
        DAGNode<String,String> node = bld.build();
        assertThat(node.getLabel(), equalTo("foo"));
        assertThat(node.getAdjacentNodes(), contains(nbr));
    }
}
