package com.hers.algorithms;

import com.hers.model.Edge;
import com.hers.model.GraphExtractor;
import com.hers.model.Node;

import java.util.*;

/**
 * A* shortest path algorithm implementation with Euclidean distance heuristic
 * Time Complexity: O((V + E) log V) - same as Dijkstra but typically explores fewer nodes
 * Space Complexity: O(V)
 */
public class AStarAlgorithm {
    
    private final GraphExtractor graph;
    private Map<Integer, Double> gScore; // actual cost from start
    private Map<Integer, Double> fScore; // gScore + heuristic
    private Map<Integer, Integer> parent;
    private Set<Integer> visited;
    private int nodesExpanded;
    
    public AStarAlgorithm(GraphExtractor graph) {
        this.graph = graph;
    }
    
    /**
     * Find shortest path from source to destination using A* with heuristic
     * @return PathResult containing path and statistics
     */
    public PathResult findPath(int source, int destination) {
        long startTime = System.nanoTime();
        
        // Initialize data structures
        gScore = new HashMap<>();
        fScore = new HashMap<>();
        parent = new HashMap<>();
        visited = new HashSet<>();
        nodesExpanded = 0;
        
        Node destNode = graph.getNode(destination);
        
        // Priority queue: (fScore, nodeId)
        PriorityQueue<NodeScore> pq = new PriorityQueue<>();
        
        // Initialize scores
        gScore.put(source, 0.0);
        fScore.put(source, heuristic(source, destination));
        pq.offer(new NodeScore(source, fScore.get(source)));
        
        while (!pq.isEmpty()) {
            NodeScore current = pq.poll();
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
                double tentativeGScore = gScore.get(currentNode) + edge.weight;
                
                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
                    parent.put(neighbor, currentNode);
                    gScore.put(neighbor, tentativeGScore);
                    double h = heuristic(neighbor, destination);
                    fScore.put(neighbor, tentativeGScore + h);
                    pq.offer(new NodeScore(neighbor, fScore.get(neighbor)));
                }
            }
        }
        
        long endTime = System.nanoTime();
        double computeTime = (endTime - startTime) / 1_000_000.0; // in milliseconds
        
        // Reconstruct path
        List<Integer> path = reconstructPath(source, destination);
        double totalDistance = calculatePathDistance(path);
        double totalTime = gScore.getOrDefault(destination, Double.MAX_VALUE);
        
        return new PathResult(path, totalTime, totalDistance, computeTime, nodesExpanded, "A*");
    }
    
    /**
     * Heuristic function: Euclidean distance converted to estimated time
     * Assumes average speed of 50 km/h (13.89 m/s)
     */
    private double heuristic(int nodeId, int destinationId) {
        Node node = graph.getNode(nodeId);
        Node dest = graph.getNode(destinationId);
        
        if (node == null || dest == null) {
            return 0.0;
        }
        
        // Haversine distance in meters
        double distance = GraphExtractor.haversineDistance(
            node.lat, node.lon, dest.lat, dest.lon
        );
        
        // Convert to estimated time (assuming 50 km/h = 13.89 m/s)
        return distance / 13.89;
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
    private static class NodeScore implements Comparable<NodeScore> {
        int nodeId;
        double fScore;
        
        NodeScore(int nodeId, double fScore) {
            this.nodeId = nodeId;
            this.fScore = fScore;
        }
        
        @Override
        public int compareTo(NodeScore other) {
            return Double.compare(this.fScore, other.fScore);
        }
    }
}