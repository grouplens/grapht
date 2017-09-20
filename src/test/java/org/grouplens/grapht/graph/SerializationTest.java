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

import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.Dependency;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.CachePolicy;
import org.grouplens.grapht.Component;
import org.grouplens.grapht.reflect.Desires;
import org.grouplens.grapht.reflect.Satisfactions;
import org.grouplens.grapht.reflect.internal.InstanceSatisfaction;
import org.grouplens.grapht.reflect.internal.types.NamedType;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.solver.DesireChain;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import javax.inject.Named;
import java.io.*;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class SerializationTest {
    private static File GRAPH_FILE = new File("graph.dump");
    
    @Test
    public void testEmptyGraph() throws Exception {
        DAGNode<Component, DesireChain> g =
                DAGNode.singleton(DependencySolver.ROOT_SATISFACTION);
        write(g);
        DAGNode<Component, DesireChain> read = read();

        assertThat(read.getReachableNodes(),
                   contains(read));
    }
    
    @Test
    public void testSharedNodesGraph() throws Exception {
        Component s1 = Component.create(Satisfactions.type(Object.class), CachePolicy.NEW_INSTANCE);
        Component s2 = Component.create(Satisfactions.type(Object.class), CachePolicy.MEMOIZE);

        DAGNode<Component, String> n2 = DAGNode.singleton(s2);
        DAGNodeBuilder<Component, String> bld = DAGNode.newBuilder(s1);
        bld.addEdge(n2, "wombat");
        bld.addEdge(n2, "foobar");
        write(bld.build());
        DAGNode<Object, Object> read = read();
        
        Assert.assertEquals(2, read.getReachableNodes().size());
        assertThat(read.getOutgoingEdges(),
                   hasSize(2));
    }
    
    @Test
    public void testDependencySolverSerialization() throws Exception {
        BindingFunctionBuilder b = new BindingFunctionBuilder();
        b.getRootContext().bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "unused").build()).to("shouldn't see this"); // extra binding to make sure it's skipped
        b.getRootContext().bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test1").build()).to("hello world");

        DependencySolver solver = DependencySolver.newBuilder()
                                                  .addBindingFunction(b.build(RuleSet.EXPLICIT))
                                                  .addBindingFunction(b.build(RuleSet.INTERMEDIATE_TYPES))
                                                  .addBindingFunction(b.build(RuleSet.SUPER_TYPES))
                                                  .addBindingFunction(DefaultDesireBindingFunction.create())
                                                  .build();
        solver.resolve(Desires.create(null, NamedType.class, false));
        
        DAGNode<Component,Dependency> g = solver.getGraph();
        write(g);
        DAGNode<Component, Dependency> root = read();
        
        Assert.assertEquals(1, root.getOutgoingEdges().size());
        DAGEdge<Component, Dependency> rootEdge = root.getOutgoingEdges().iterator().next();
        DAGNode<Component, Dependency> namedType = rootEdge.getTail();
        
        Assert.assertEquals(NamedType.class, namedType.getLabel().getSatisfaction().getErasedType());
        Assert.assertEquals(NamedType.class, rootEdge.getLabel().getInitialDesire().getDesiredType());
        Assert.assertEquals(rootEdge.getLabel().getInitialDesire().getSatisfaction(), namedType.getLabel().getSatisfaction());
        Assert.assertNull(rootEdge.getLabel().getInitialDesire().getInjectionPoint().getQualifier());
        Assert.assertTrue(rootEdge.getLabel().getInitialDesire().getInjectionPoint().getAttributes().isEmpty());
        
        Assert.assertEquals(1, namedType.getOutgoingEdges().size());
        DAGEdge<Component, Dependency> nameEdge = namedType.getOutgoingEdges().iterator().next();
        DAGNode<Component, Dependency> string = nameEdge.getTail();
        
        Assert.assertEquals(String.class, string.getLabel().getSatisfaction().getErasedType());
        Assert.assertEquals(String.class, nameEdge.getLabel().getInitialDesire().getDesiredType());
        Assert.assertEquals(AnnotationBuilder.of(Named.class).setValue("test1").build(), nameEdge.getLabel().getInitialDesire().getInjectionPoint().getQualifier());
        Assert.assertTrue(nameEdge.getLabel().getInitialDesire().getInjectionPoint().getAttributes().isEmpty());
        
        Assert.assertTrue(string.getLabel().getSatisfaction() instanceof InstanceSatisfaction);
        Assert.assertEquals("hello world", ((InstanceSatisfaction) string.getLabel().getSatisfaction()).getInstance());
    }
    
    @After
    public void cleanup() throws Exception {
        GRAPH_FILE.delete();
    }
    
    private <V, E> void write(DAGNode<V, E> g) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(GRAPH_FILE));
        out.writeObject(g);
        out.flush();
        out.close();
    }
    
    private <V,E> DAGNode<V,E> read() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(GRAPH_FILE));
        try {
            DAGNode<V,E> g = (DAGNode<V,E>) in.readObject();
            return g;
        } finally {
            in.close();
        }
    }
}
