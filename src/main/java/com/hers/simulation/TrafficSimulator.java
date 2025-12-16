package com.hers.simulation;

import com.hers.model.Edge;
import com.hers.model.GraphExtractor;

import java.util.*;

/**
 * Simulates dynamic traffic conditions including:
 * - Traffic congestion (increased edge weights)
 * - Road blockages (blocked edges)
 * - Traffic clearance (restored normal conditions)
 */
public class TrafficSimulator {
    
    private final GraphExtractor graph;
    private final Map<String, EdgeState> edgeStates;
    private final Random random;
    
    public TrafficSimulator(GraphExtractor graph) {
        this.graph = graph;
        this.edgeStates = new HashMap<>();
        this.random = new Random(42); // Fixed seed for reproducibility
    }
    
    /**
     * Block a specific road segment
     */
    public void blockRoad(int fromNode, int toNode, String reason) {
        List<Edge> neighbors = graph.getNeighbors(fromNode);
        for (Edge edge : neighbors) {
            if (edge.toNode == toNode) {
                edge.block();
                String key = fromNode + "->" + toNode;
                edgeStates.put(key, new EdgeState(fromNode, toNode, true, 1.0, reason));
                System.out.println("🚧 BLOCKED: Road from " + fromNode + " to " + toNode + " (" + reason + ")");
                return;
            }
        }
    }
    
    /**
     * Unblock a specific road segment
     */
    public void unblockRoad(int fromNode, int toNode) {
        List<Edge> neighbors = graph.getNeighbors(fromNode);
        for (Edge edge : neighbors) {
            if (edge.toNode == toNode) {
                edge.unblock();
                String key = fromNode + "->" + toNode;
                edgeStates.remove(key);
                System.out.println("✅ CLEARED: Road from " + fromNode + " to " + toNode);
                return;
            }
        }
    }
    
    /**
     * Apply traffic congestion to a road segment
     * @param multiplier traffic multiplier (1.0 = normal, 2.0 = double time, 3.0 = triple time)
     */
    public void applyTraffic(int fromNode, int toNode, double multiplier, String severity) {
        List<Edge> neighbors = graph.getNeighbors(fromNode);
        for (Edge edge : neighbors) {
            if (edge.toNode == toNode) {
                edge.updateTraffic(multiplier);
                String key = fromNode + "->" + toNode;
                edgeStates.put(key, new EdgeState(fromNode, toNode, false, multiplier, severity));
                System.out.println("🚗 TRAFFIC: " + severity + " on road " + fromNode + " -> " + toNode + 
                                 " (x" + multiplier + " slower)");
                return;
            }
        }
    }
    
    /**
     * Clear traffic congestion (restore normal speed)
     */
    public void clearTraffic(int fromNode, int toNode) {
        List<Edge> neighbors = graph.getNeighbors(fromNode);
        for (Edge edge : neighbors) {
            if (edge.toNode == toNode) {
                edge.updateTraffic(1.0);
                String key = fromNode + "->" + toNode;
                edgeStates.remove(key);
                System.out.println("✅ TRAFFIC CLEARED: Road " + fromNode + " -> " + toNode);
                return;
            }
        }
    }
    
    /**
     * Block multiple roads along a path (simulate accident or construction)
     */
    public void blockPathSegment(List<Integer> path, int startIndex, int endIndex, String reason) {
        System.out.println("\n🚧 INCIDENT: " + reason);
        for (int i = startIndex; i < endIndex && i < path.size() - 1; i++) {
            blockRoad(path.get(i), path.get(i + 1), reason);
        }
    }
    
    /**
     * Apply heavy traffic to multiple road segments
     */
    public void applyTrafficJam(List<Integer> path, int startIndex, int endIndex, double multiplier) {
        System.out.println("\n🚗 TRAFFIC JAM: Heavy congestion detected");
        for (int i = startIndex; i < endIndex && i < path.size() - 1; i++) {
            applyTraffic(path.get(i), path.get(i + 1), multiplier, "Heavy");
        }
    }
    
    /**
     * Get current traffic state summary
     */
    public void printTrafficState() {
        if (edgeStates.isEmpty()) {
            System.out.println("✅ All roads clear - no traffic incidents");
            return;
        }
        
        System.out.println("\n📊 Current Traffic State:");
        System.out.println("Total affected road segments: " + edgeStates.size());
        
        long blocked = edgeStates.values().stream().filter(s -> s.blocked).count();
        long congested = edgeStates.values().stream().filter(s -> !s.blocked).count();
        
        System.out.println("  - Blocked roads: " + blocked);
        System.out.println("  - Congested roads: " + congested);
        
        System.out.println("\nDetails:");
        for (EdgeState state : edgeStates.values()) {
            System.out.println("  " + state);
        }
    }
    
    /**
     * Clear all traffic conditions
     */
    public void clearAllTraffic() {
        System.out.println("\n🔄 Clearing all traffic conditions...");
        
        // Create a copy to avoid ConcurrentModificationException
        List<EdgeState> statesToClear = new ArrayList<>(edgeStates.values());
        
        for (EdgeState state : statesToClear) {
            List<Edge> neighbors = graph.getNeighbors(state.fromNode);
            for (Edge edge : neighbors) {
                if (edge.toNode == state.toNode) {
                    if (state.blocked) {
                        edge.unblock();
                    } else {
                        edge.updateTraffic(1.0);
                    }
                    break;
                }
            }
        }
        
        edgeStates.clear();
        System.out.println("✅ All traffic cleared");
    }
    
    /**
     * Stores the state of an edge
     */
    private static class EdgeState {
        int fromNode;
        int toNode;
        boolean blocked;
        double trafficMultiplier;
        String reason;
        
        EdgeState(int fromNode, int toNode, boolean blocked, double trafficMultiplier, String reason) {
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.blocked = blocked;
            this.trafficMultiplier = trafficMultiplier;
            this.reason = reason;
        }
        
        @Override
        public String toString() {
            if (blocked) {
                return "🚧 " + fromNode + " -> " + toNode + ": BLOCKED (" + reason + ")";
            } else {
                return "🚗 " + fromNode + " -> " + toNode + ": " + reason + " (x" + trafficMultiplier + ")";
            }
        }
    }
}