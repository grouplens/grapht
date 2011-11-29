package org.grouplens.inject.graph;

/**
 * Edge is an immutable construct between two Nodes, the head and the tail. An
 * Edge is used to represent a directional graph, connecting any two nodes
 * within the graph. Thus, an edge from X to Y is different from an edge from Y
 * to X. Additionally, the edge stores a desire representing the relationship
 * between the head and tail node (generally for dependency resolution).
 * 
 * @author Michael Ludwig
 */
public class Edge {
    private final Node head;
    private final Node tail;
    
    private final Desire desire;

    /**
     * Create a new Edge between the two Nodes, source'ed at <tt>head</tt> and
     * ending at <tt>tail</tt>. The provided Desire is the desire associated
     * with the edge.
     * 
     * @param head The start or head node of the edge
     * @param tail The end or tail node of the edge
     * @param desire The desire along the edge
     */
    public Edge(Node head, Node tail, Desire desire) {
        this.head = head;
        this.tail = tail;
        this.desire = desire;
    }

    /**
     * Get the head node of this edge. Edges are uni-directional, so the edge is
     * source'ed at the head and connects to the tail. The edge can be
     * interpreted as "the head node desires the tail".
     * 
     * @return The head node of the edge
     */
    public Node getHead() {
        return head;
    }

    /**
     * Get the tail node of this edge. Edges are uni-directional, so the edge
     * ends at the tail and comes from the head. The edge can be interpreted as
     * "the tail node is desired by the head".
     * 
     * @return The tail node of the edge
     */
    public Node getTail() {
        return tail;
    }

    /**
     * Get the Desire associated with this edge. It is generally assumed that
     * the desire is a dependency from the head to the tail, but it depends on
     * the context that the graph is used in.
     * 
     * @return The desire on this edge
     */
    public Desire getDesire() {
        return desire;
    }
    
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Edge))
            return false;
        
        Edge t = (Edge) o;
        return t.head.equals(head) && 
               t.tail.equals(tail) && 
               t.desire.equals(desire);
    }
    
    @Override
    public int hashCode() {
        int result = 37;
        result += 17 * head.hashCode();
        result += 17 * tail.hashCode();
        result += 17 * desire.hashCode();
        return result;
    }
}
