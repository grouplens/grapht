package org.grouplens.inject.graph;

public class Edge {
    private final Node head;
    private final Node tail;
    
    private final Desire desire;
    
    public Edge(Node head, Node tail, Desire desire) {
        this.head = head;
        this.tail = tail;
        this.desire = desire;
    }
    
    public Node getHeadNode() {
        return head;
    }
    
    public Node getTailNode() {
        return tail;
    }
    
    public Desire getDesire() {
        return desire;
    }
}
