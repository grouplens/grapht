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
package org.grouplens.grapht.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import javax.inject.Named;

import org.grouplens.grapht.BindingFunctionBuilder;
import org.grouplens.grapht.BindingFunctionBuilder.RuleSet;
import org.grouplens.grapht.annotation.AnnotationBuilder;
import org.grouplens.grapht.solver.DefaultDesireBindingFunction;
import org.grouplens.grapht.solver.DependencySolver;
import org.grouplens.grapht.spi.Desire;
import org.grouplens.grapht.spi.Satisfaction;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.types.NamedType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class SerializationTest {
    private static File GRAPH_FILE = new File("graph.dump");
    
    @Test
    public void testEmptyGraph() throws Exception {
        Graph<String, String> g = new Graph<String, String>();
        write(g);
        Graph<String, String> read = read();
        
        Assert.assertTrue(read.getNodes().isEmpty());
    }
    
    @Test
    public void testSingleNodeGraph() throws Exception {
        Graph<String, String> g = new Graph<String, String>();
        g.addNode(new Node<String>("hello"));
        write(g);
        Graph<String, String> read = read();
        
        Assert.assertEquals(1, read.getNodes().size());
        Assert.assertNotNull(read.getNode("hello"));
    }
    
    @Test
    public void testSingleEdgeGraph() throws Exception {
        Graph<String, String> g = new Graph<String, String>();
        Node<String> n1 = new Node<String>("hello");
        Node<String> n2 = new Node<String>("world");
        g.addEdge(new Edge<String, String>(n1, n2, "!"));
        write(g);
        Graph<String, String> read = read();
        
        Assert.assertEquals(2, read.getNodes().size());
        n1 = read.getNode("hello");
        n2 = read.getNode("world");
        Assert.assertEquals(1, read.getEdges(n1, n2).size());
        Assert.assertEquals("!", read.getEdges(n1, n2).iterator().next().getLabel());
    }
    
    @Test
    public void testManyEdgesGraph() throws Exception {
        Graph<String, String> g = new Graph<String, String>();
        Node<String> n1 = new Node<String>("hello");
        Node<String> n2 = new Node<String>("world");
        Node<String> n3 = new Node<String>("goodbye");
        g.addEdge(new Edge<String, String>(n1, n2, "!"));
        g.addEdge(new Edge<String, String>(n1, n3, "@"));
        write(g);
        Graph<String, String> read = read();
        
        Assert.assertEquals(3, read.getNodes().size());
        n1 = read.getNode("hello");
        n2 = read.getNode("world");
        n3 = read.getNode("goodbye");
        Assert.assertEquals(2, read.getOutgoingEdges(n1).size());
        Assert.assertSame(n2, read.getOutgoingEdge(n1, "!").getTail());
        Assert.assertSame(n3, read.getOutgoingEdge(n1, "@").getTail());
    }
    
    @Test
    public void testSharedNodesGraph() throws Exception {
        Graph<String, String> g = new Graph<String, String>();
        Node<String> n1 = new Node<String>("hello");
        Node<String> n2 = new Node<String>("world");
        g.addEdge(new Edge<String, String>(n1, n2, "!"));
        g.addEdge(new Edge<String, String>(n1, n2, "@"));
        write(g);
        Graph<String, String> read = read();
        
        Assert.assertEquals(2, read.getNodes().size());
        n1 = read.getNode("hello");
        n2 = read.getNode("world");
        Assert.assertEquals(2, read.getOutgoingEdges(n1).size());
        Assert.assertEquals(2, read.getEdges(n1, n2).size());
    }
    
    @Test
    public void testNullLabels() throws Exception {
        Graph<String, String> g = new Graph<String, String>();
        Node<String> n1 = new Node<String>("root");
        Node<String> n2 = new Node<String>(null);
        g.addEdge(new Edge<String, String>(n1, n2, null));
        write(g);
        Graph<String, String> read = read();
        
        Assert.assertEquals(2, read.getNodes().size());
        n1 = read.getNode("root");
        n2 = read.getNode(null);
        Assert.assertEquals(1, read.getEdges(n1, n2).size());
        Assert.assertEquals(null, read.getEdges(n1, n2).iterator().next().getLabel());
    }
    
    @Test
    public void testDependencySolverSerialization() throws Exception {
        BindingFunctionBuilder b = new BindingFunctionBuilder();
        b.getRootContext().bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "unused").build()).to("shouldn't see this"); // extra binding to make sure it's skipped
        b.getRootContext().bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test1").build()).to("hello world");
        
        DependencySolver solver = new DependencySolver(Arrays.asList(b.getFunction(RuleSet.EXPLICIT), 
                                                                     b.getFunction(RuleSet.INTERMEDIATE_TYPES), 
                                                                     b.getFunction(RuleSet.SUPER_TYPES), 
                                                                     new DefaultDesireBindingFunction(b.getSPI())), 100);
        solver.resolve(b.getSPI().desire(null, NamedType.class, false));
        
        Graph<Satisfaction, Desire> g = solver.getGraph();
        write(g);
        Graph<Satisfaction, Desire> read = read();
        
        Node<Satisfaction> root = read.getNode(null);
        Assert.assertEquals(1, read.getOutgoingEdges(root).size());
        Edge<Satisfaction, Desire> rootEdge = read.getOutgoingEdges(root).iterator().next();
        Node<Satisfaction> namedType = rootEdge.getTail();
        
        Assert.assertEquals(NamedType.class, namedType.getLabel().getErasedType());
        Assert.assertEquals(NamedType.class, rootEdge.getLabel().getDesiredType());
        Assert.assertEquals(rootEdge.getLabel().getSatisfaction(), namedType.getLabel());
        Assert.assertNull(rootEdge.getLabel().getInjectionPoint().getAttributes().getQualifier());
        Assert.assertTrue(rootEdge.getLabel().getInjectionPoint().getAttributes().getAttributes().isEmpty());
        
        Assert.assertEquals(1, read.getOutgoingEdges(namedType).size());
        Edge<Satisfaction, Desire> nameEdge = read.getOutgoingEdges(namedType).iterator().next();
        Node<Satisfaction> string = nameEdge.getTail();
        
        Assert.assertEquals(String.class, string.getLabel().getErasedType());
        Assert.assertEquals(String.class, nameEdge.getLabel().getDesiredType());
        Assert.assertEquals(AnnotationBuilder.of(Named.class).setValue("test1").build(), nameEdge.getLabel().getInjectionPoint().getAttributes().getQualifier());
        Assert.assertTrue(nameEdge.getLabel().getInjectionPoint().getAttributes().getAttributes().isEmpty());
        
        Assert.assertTrue(string.getLabel() instanceof InstanceSatisfaction);
        Assert.assertEquals("hello world", ((InstanceSatisfaction) string.getLabel()).getInstance());
    }
    
    @After
    public void cleanup() throws Exception {
        GRAPH_FILE.delete();
    }
    
    private <N, E> void write(Graph<N, E> g) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(GRAPH_FILE));
        out.writeObject(g);
        out.flush();
        out.close();
    }
    
    @SuppressWarnings("unchecked")
    private <N, E> Graph<N, E> read() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(GRAPH_FILE));
        Graph<N, E> g = (Graph<N, E>) in.readObject();
        in.close();
        return g;
    }
}
