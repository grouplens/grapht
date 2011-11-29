package org.grouplens.inject.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final Map<Node, List<Edge>> graph;
    
    public Graph() {
        graph = new HashMap<Node, List<Edge>>();
    }
    
    public Collection<Node> getNodes() {
        return Collections.unmodifiableSet(graph.keySet());
    }
    
    public Collection<Edge> getOutgoingEdges(Node node) {
        // This will return null if the graph does not contain
        // the given node.
        return Collections.unmodifiableList(graph.get(node));
    }
    
    public void addNode(Node node) {
        if (!graph.containsKey(node)) {
            // add the node, with 0 edges
            graph.put(node, new ArrayList<Edge>());
        }
    }
    
    public void removeNode(Node node) {
        graph.remove(node);
    }
    
    public void replaceNode(Node oldNode, Node newNode) {
        
    }
    
    public void addEdge(Edge edge) {
        List<Edge> edges = graph.get(edge.getHeadNode());
        if (edges == null) {
            // new head node, too
            edges = new ArrayList<Edge>();
            graph.put(edge.getHeadNode(), edges);
        }
        
        edges.add(edge);
        
        if (!graph.containsKey(edge.getTailNode())) {
            // new tail node
            graph.put(edge.getTailNode(), new ArrayList<Edge>());
        }
    }
    
    public void removeEdge(Edge edge) {
        List<Edge> edges = graph.get(edge.getHeadNode());
        if (edges != null) {
            edges.remove(edge);
        }
    }
    
    public void replaceEdge(Edge oldEdge, Edge newEdge) {
        
    }
}
