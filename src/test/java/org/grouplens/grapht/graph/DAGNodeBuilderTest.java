/*
 * Grapht, an open source dependency injector.
 * Copyright 2014-2015 various contributors (see CONTRIBUTORS.txt)
 * Copyright 2010-2014 Regents of the University of Minnesota
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
