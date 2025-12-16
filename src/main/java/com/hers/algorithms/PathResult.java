package com.hers.algorithms;

import java.util.List;

/**
 * Result of a pathfinding operation including statistics
 */
public class PathResult {
    public final List<Integer> path;           // sequence of node IDs
    public final double totalTime;             // in seconds
    public final double totalDistance;         // in meters
    public final double computeTimeMs;         // algorithm runtime in milliseconds
    public final int nodesExpanded;            // number of nodes explored
    public final String algorithmUsed;         // "Dijkstra" or "A*"
    
    public PathResult(List<Integer> path, double totalTime, double totalDistance,
                      double computeTimeMs, int nodesExpanded, String algorithmUsed) {
        this.path = path;
        this.totalTime = totalTime;
        this.totalDistance = totalDistance;
        this.computeTimeMs = computeTimeMs;
        this.nodesExpanded = nodesExpanded;
        this.algorithmUsed = algorithmUsed;
    }
    
    public boolean isPathFound() {
        return !path.isEmpty();
    }
    
    @Override
    public String toString() {
        if (!isPathFound()) {
            return "No path found!";
        }
        
        return String.format(
            "=== %s Result ===\n" +
            "Path nodes: %d\n" +
            "Total distance: %.2f meters (%.2f km)\n" +
            "Estimated time: %.2f seconds (%.2f minutes)\n" +
            "Nodes expanded: %d\n" +
            "Compute time: %.3f ms",
            algorithmUsed,
            path.size(),
            totalDistance,
            totalDistance / 1000.0,
            totalTime,
            totalTime / 60.0,
            nodesExpanded,
            computeTimeMs
        );
    }
    
    /**
     * Get path as string of node IDs
     */
    public String getPathString() {
        if (path.isEmpty()) {
            return "[]";
        }
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(10, path.size()); i++) {
            sb.append(path.get(i));
            if (i < Math.min(10, path.size()) - 1) {
                sb.append(" → ");
            }
        }
        if (path.size() > 10) {
            sb.append(" ... → ").append(path.get(path.size() - 1));
        }
        sb.append("]");
        return sb.toString();
    }
}