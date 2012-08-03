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
import org.grouplens.grapht.spi.CachePolicy;
import org.grouplens.grapht.spi.CachedSatisfaction;
import org.grouplens.grapht.spi.InjectSPI;
import org.grouplens.grapht.spi.reflect.InstanceSatisfaction;
import org.grouplens.grapht.spi.reflect.ReflectionInjectSPI;
import org.grouplens.grapht.spi.reflect.types.NamedType;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class SerializationTest {
    private static File GRAPH_FILE = new File("graph.dump");
    
    @Test
    public void testEmptyGraph() throws Exception {
        Graph g = new Graph();
        write(g);
        Graph read = read();
        
        Assert.assertTrue(read.getNodes().isEmpty());
    }
    
    @Test
    public void testSharedNodesGraph() throws Exception {
        InjectSPI spi = new ReflectionInjectSPI();
        CachedSatisfaction s1 = new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE);
        CachedSatisfaction s2 = new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.MEMOIZE);
        
        Graph g = new Graph();
        Node n1 = new Node(s1);
        Node n2 = new Node(s2);
        g.addEdge(new Edge(n1, n2, null));
        g.addEdge(new Edge(n1, n2, null));
        write(g);
        Graph read = read();
        
        Assert.assertEquals(2, read.getNodes().size());
        n1 = read.getNode(s1);
        n2 = read.getNode(s2);
        Assert.assertEquals(2, read.getOutgoingEdges(n1).size());
        Assert.assertEquals(2, read.getEdges(n1, n2).size());
    }
    
    @Test
    public void testNullLabels() throws Exception {
        InjectSPI spi = new ReflectionInjectSPI();
        CachedSatisfaction rootLabel = new CachedSatisfaction(spi.satisfy(Object.class), CachePolicy.NEW_INSTANCE);
        Graph g = new Graph();
        Node n1 = new Node(rootLabel);
        Node n2 = new Node(null);
        g.addEdge(new Edge(n1, n2, null));
        write(g);
        Graph read = read();
        
        Assert.assertEquals(2, read.getNodes().size());
        n1 = read.getNode(rootLabel);
        n2 = read.getNode(null);
        Assert.assertEquals(1, read.getEdges(n1, n2).size());
        Assert.assertEquals(null, read.getEdges(n1, n2).iterator().next().getLabel());
    }
    
    @Test
    public void testDependencySolverSerialization() throws Exception {
        BindingFunctionBuilder b = new BindingFunctionBuilder();
        b.getRootContext().bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "unused").build()).to("shouldn't see this"); // extra binding to make sure it's skipped
        b.getRootContext().bind(String.class).withQualifier(new AnnotationBuilder<Named>(Named.class).set("value", "test1").build()).to("hello world");
        
        DependencySolver solver = new DependencySolver(Arrays.asList(b.build(RuleSet.EXPLICIT),
                                                                     b.build(RuleSet.INTERMEDIATE_TYPES),
                                                                     b.build(RuleSet.SUPER_TYPES),
                                                                     new DefaultDesireBindingFunction(b.getSPI())), 100);
        solver.resolve(b.getSPI().desire(null, NamedType.class, false));
        
        Graph g = solver.getGraph();
        write(g);
        Graph read = read();
        
        Node root = read.getNode(null);
        Assert.assertEquals(1, read.getOutgoingEdges(root).size());
        Edge rootEdge = read.getOutgoingEdges(root).iterator().next();
        Node namedType = rootEdge.getTail();
        
        Assert.assertEquals(NamedType.class, namedType.getLabel().getSatisfaction().getErasedType());
        Assert.assertEquals(NamedType.class, rootEdge.getDesire().getDesiredType());
        Assert.assertEquals(rootEdge.getDesire().getSatisfaction(), namedType.getLabel().getSatisfaction());
        Assert.assertNull(rootEdge.getDesire().getInjectionPoint().getAttributes().getQualifier());
        Assert.assertTrue(rootEdge.getDesire().getInjectionPoint().getAttributes().getAttributes().isEmpty());
        
        Assert.assertEquals(1, read.getOutgoingEdges(namedType).size());
        Edge nameEdge = read.getOutgoingEdges(namedType).iterator().next();
        Node string = nameEdge.getTail();
        
        Assert.assertEquals(String.class, string.getLabel().getSatisfaction().getErasedType());
        Assert.assertEquals(String.class, nameEdge.getDesire().getDesiredType());
        Assert.assertEquals(AnnotationBuilder.of(Named.class).setValue("test1").build(), nameEdge.getDesire().getInjectionPoint().getAttributes().getQualifier());
        Assert.assertTrue(nameEdge.getDesire().getInjectionPoint().getAttributes().getAttributes().isEmpty());
        
        Assert.assertTrue(string.getLabel().getSatisfaction() instanceof InstanceSatisfaction);
        Assert.assertEquals("hello world", ((InstanceSatisfaction) string.getLabel().getSatisfaction()).getInstance());
    }
    
    @After
    public void cleanup() throws Exception {
        GRAPH_FILE.delete();
    }
    
    private <N, E> void write(Graph g) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(GRAPH_FILE));
        out.writeObject(g);
        out.flush();
        out.close();
    }
    
    private Graph read() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(GRAPH_FILE));
        Graph g = (Graph) in.readObject();
        in.close();
        return g;
    }
}
