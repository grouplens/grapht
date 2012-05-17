package org.grouplens.grapht.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    
    @After
    public void cleanup() throws Exception {
        GRAPH_FILE.delete();
    }
    
    private void write(Graph<String, String> g) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(GRAPH_FILE));
        out.writeObject(g);
        out.flush();
        out.close();
    }
    
    @SuppressWarnings("unchecked")
    private Graph<String, String> read() throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(GRAPH_FILE));
        return (Graph<String, String>) in.readObject();
    }
}
