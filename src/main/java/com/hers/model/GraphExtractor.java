package com.hers.model;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.BaseGraph;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.util.EdgeIterator;

import java.util.*;

/**
 * Extracts a simplified graph structure from GraphHopper
 * for use with custom routing algorithms (Dijkstra, A*)
 */
public class GraphExtractor {
    
    private final Map<Integer, Node> nodes;
    private final Map<Integer, List<Edge>> adjacencyList;
    private final GraphHopper hopper;
    private final BaseGraph graph;
    private final NodeAccess nodeAccess;
    
    public GraphExtractor(GraphHopper hopper) {
        this.hopper = hopper;
        this.graph = hopper.getBaseGraph();
        this.nodeAccess = graph.getNodeAccess();
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        extractGraph();
    }
    
    /**
     * Extract all nodes and edges from GraphHopper
     */
    private void extractGraph() {
        System.out.println("Extracting graph from GraphHopper...");
        int nodeCount = graph.getNodes();
        
        // Extract all nodes
        for (int i = 0; i < nodeCount; i++) {
            double lat = nodeAccess.getLat(i);
            double lon = nodeAccess.getLon(i);
            nodes.put(i, new Node(i, lat, lon));
            adjacencyList.put(i, new ArrayList<>());
        }
        
        // Extract all edges
        int edgeCount = 0;
        for (int nodeId = 0; nodeId < nodeCount; nodeId++) {
            EdgeIterator iter = graph.createEdgeExplorer().setBaseNode(nodeId);
            while (iter.next()) {
                int toNode = iter.getAdjNode();
                double distance = iter.getDistance(); // in meters
                
                // Calculate time based on distance and assumed average speed
                // Assume 50 km/h average speed = 13.89 m/s
                double time = distance / 13.89; // time in seconds
                
                // Create edge (weight = time in seconds for routing)
                Edge edge = new Edge(toNode, time, distance, nodeId);
                adjacencyList.get(nodeId).add(edge);
                edgeCount++;
            }
        }
        
        System.out.println("Graph extracted: " + nodes.size() + " nodes, " + edgeCount + " edges");
    }
    
    /**
     * Find nearest node to given coordinates
     */
    public int findNearestNode(double lat, double lon) {
        int nearestNode = -1;
        double minDist = Double.MAX_VALUE;
        
        for (Map.Entry<Integer, Node> entry : nodes.entrySet()) {
            Node node = entry.getValue();
            double dist = haversineDistance(lat, lon, node.lat, node.lon);
            if (dist < minDist) {
                minDist = dist;
                nearestNode = entry.getKey();
            }
        }
        
        return nearestNode;
    }
    
    /**
     * Get neighbors of a node
     */
    public List<Edge> getNeighbors(int nodeId) {
        return adjacencyList.getOrDefault(nodeId, new ArrayList<>());
    }
    
    /**
     * Get node by ID
     */
    public Node getNode(int nodeId) {
        return nodes.get(nodeId);
    }
    
    /**
     * Get all nodes
     */
    public Map<Integer, Node> getAllNodes() {
        return nodes;
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     */
    public static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000; // Earth radius in meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    /**
     * Get total number of nodes
     */
    public int getNodeCount() {
        return nodes.size();
    }
    
    /**
     * Get total number of edges
     */
    public int getEdgeCount() {
        return adjacencyList.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}