package com.hers.model;

/**
 * Represents a node (intersection) in the road network
 */
public class Node {
    public final int id;
    public final double lat;
    public final double lon;
    
    public Node(int id, double lat, double lon) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
    }
    
    @Override
    public String toString() {
        return "Node{id=" + id + ", lat=" + lat + ", lon=" + lon + "}";
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return id == node.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}