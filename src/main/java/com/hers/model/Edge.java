package com.hers.model;

/**
 * Represents a directed edge (road segment) in the road network
 */
public class Edge {
    public final int toNode;      // destination node ID
    public double weight;          // travel time in seconds (can be updated for traffic)
    public final double distance;  // physical distance in meters
    public final int fromNode;     // source node ID
    public boolean blocked;        // true if road is blocked
    
    public Edge(int toNode, double weight, double distance, int fromNode) {
        this.toNode = toNode;
        this.weight = weight;
        this.distance = distance;
        this.fromNode = fromNode;
        this.blocked = false;
    }
    
    /**
     * Update edge weight (simulate traffic congestion)
     * @param multiplier traffic multiplier (1.0 = normal, 2.0 = double time, etc.)
     */
    public void updateTraffic(double multiplier) {
        this.weight = (distance / 13.89) * multiplier; // 13.89 m/s ≈ 50 km/h base speed
    }
    
    /**
     * Block this road segment
     */
    public void block() {
        this.blocked = true;
    }
    
    /**
     * Unblock this road segment
     */
    public void unblock() {
        this.blocked = false;
    }
    
    @Override
    public String toString() {
        return "Edge{from=" + fromNode + ", to=" + toNode + 
               ", weight=" + String.format("%.2f", weight) + "s" +
               ", distance=" + String.format("%.2f", distance) + "m" +
               (blocked ? " [BLOCKED]" : "") + "}";
    }
}