package com.hers.algorithms;

import com.hers.model.Edge;
import com.hers.model.GraphExtractor;
import com.hers.model.Node;

import java.util.*;

/**
 * Dijkstra's shortest path algorithm implementation
 * Time Complexity: O((V + E) log V) with priority queue
 * Space Complexity: O(V)
 */
public class DijkstraAlgorithm {
    
    private final GraphExtractor graph;
    private Map<Integer, Double> distance;
    private Map<Integer, Integer> parent;
    private Set<Integer> visited;
    private int nodesExpanded;
    
    public DijkstraAlgorithm(GraphExtractor graph) {
        this.graph = graph;
    }
    
    /**
     * Find shortest path from source to destination
     * @return PathResult containing path and statistics
     */
    public PathResult findPath(int source, int destination) {
        long startTime = System.nanoTime();
        
        // Initialize data structures
        distance = new HashMap<>();
        parent = new HashMap<>();
        visited = new HashSet<>();
        nodesExpanded = 0;
        
        // Priority queue: (distance, nodeId)
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>();
        
        // Initialize distances to infinity
        distance.put(source, 0.0);
        pq.offer(new NodeDistance(source, 0.0));
        
        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            int currentNode = current.nodeId;
            
            // Skip if already visited
            if (visited.contains(currentNode)) {
                continue;
            }
            
            visited.add(currentNode);
            nodesExpanded++;
            
            // Found destination
            if (currentNode == destination) {
                break;
            }
            
            // Explore neighbors
            for (Edge edge : graph.getNeighbors(currentNode)) {
                // Skip blocked roads
                if (edge.blocked) {
                    continue;
                }
                
                int neighbor = edge.toNode;
                double newDist = distance.get(currentNode) + edge.weight;
                
                if (!distance.containsKey(neighbor) || newDist < distance.get(neighbor)) {
                    distance.put(neighbor, newDist);
                    parent.put(neighbor, currentNode);
                    pq.offer(new NodeDistance(neighbor, newDist));
                }
            }
        }
        
        long endTime = System.nanoTime();
        double computeTime = (endTime - startTime) / 1_000_000.0; // in milliseconds
        
        // Reconstruct path
        List<Integer> path = reconstructPath(source, destination);
        double totalDistance = calculatePathDistance(path);
        double totalTime = distance.getOrDefault(destination, Double.MAX_VALUE);
        
        return new PathResult(path, totalTime, totalDistance, computeTime, nodesExpanded, "Dijkstra");
    }
    
    /**
     * Reconstruct path from source to destination using parent map
     */
    private List<Integer> reconstructPath(int source, int destination) {
        if (!parent.containsKey(destination) && source != destination) {
            return new ArrayList<>(); // No path found
        }
        
        Stack<Integer> stack = new Stack<>();
        int current = destination;
        
        while (current != source) {
            stack.push(current);
            if (!parent.containsKey(current)) {
                return new ArrayList<>(); // Path broken
            }
            current = parent.get(current);
        }
        stack.push(source);
        
        List<Integer> path = new ArrayList<>();
        while (!stack.isEmpty()) {
            path.add(stack.pop());
        }
        
        return path;
    }
    
    /**
     * Calculate total physical distance of path
     */
    private double calculatePathDistance(List<Integer> path) {
        double totalDist = 0.0;
        
        for (int i = 0; i < path.size() - 1; i++) {
            int from = path.get(i);
            int to = path.get(i + 1);
            
            for (Edge edge : graph.getNeighbors(from)) {
                if (edge.toNode == to) {
                    totalDist += edge.distance;
                    break;
                }
            }
        }
        
        return totalDist;
    }
    
    /**
     * Helper class for priority queue
     */
    private static class NodeDistance implements Comparable<NodeDistance> {
        int nodeId;
        double distance;
        
        NodeDistance(int nodeId, double distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }
        
        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(this.distance, other.distance);
        }
    }
}