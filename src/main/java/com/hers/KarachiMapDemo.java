package com.hers;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.util.GHUtility;
import com.hers.algorithms.AStarAlgorithm;
import com.hers.algorithms.DijkstraAlgorithm;
import com.hers.algorithms.PathResult;
import com.hers.model.GraphExtractor;

/**
 * Healthcare Emergency Routing System (HERS) Demo
 * Demonstrates custom Dijkstra and A* algorithms on real Karachi map data
 */
public class KarachiMapDemo {

    public static void main(String[] args) {
        System.out.println("==============================================");
        System.out.println("Healthcare Emergency Routing System (HERS)");
        System.out.println("==============================================\n");

        // Step 1: Load GraphHopper with Pakistan OSM data
        System.out.println("Step 1: Loading OSM map data...");
        GraphHopper hopper = loadGraphHopper();
        
        // Step 2: Extract graph for our custom algorithms
        System.out.println("\nStep 2: Extracting graph structure...");
        GraphExtractor graphExtractor = new GraphExtractor(hopper);
        System.out.println("Graph statistics:");
        System.out.println("  - Total nodes: " + graphExtractor.getNodeCount());
        System.out.println("  - Total edges: " + graphExtractor.getEdgeCount());

        // Step 3: Define emergency scenario
        System.out.println("\n==============================================");
        System.out.println("EMERGENCY SCENARIO: Ambulance Dispatch");
        System.out.println("==============================================");
        
        // Clifton (ambulance location) → Jinnah Hospital
        double ambulanceLat = 24.8607, ambulanceLon = 67.0011; // Clifton
        double hospitalLat = 24.8609, hospitalLon = 67.0300;   // Saddar area
        
        System.out.println("Ambulance location: Clifton (" + ambulanceLat + ", " + ambulanceLon + ")");
        System.out.println("Hospital location: Saddar (" + hospitalLat + ", " + hospitalLon + ")");
        
        // Find nearest nodes
        int sourceNode = graphExtractor.findNearestNode(ambulanceLat, ambulanceLon);
        int destNode = graphExtractor.findNearestNode(hospitalLat, hospitalLon);
        
        System.out.println("\nNearest graph nodes:");
        System.out.println("  - Source node ID: " + sourceNode);
        System.out.println("  - Destination node ID: " + destNode);

        // Step 4: Run Dijkstra
        System.out.println("\n==============================================");
        System.out.println("Running Dijkstra's Algorithm...");
        System.out.println("==============================================");
        DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graphExtractor);
        PathResult dijkstraResult = dijkstra.findPath(sourceNode, destNode);
        System.out.println(dijkstraResult);
        System.out.println("Path: " + dijkstraResult.getPathString());

        // Step 5: Run A*
        System.out.println("\n==============================================");
        System.out.println("Running A* Algorithm...");
        System.out.println("==============================================");
        AStarAlgorithm aStar = new AStarAlgorithm(graphExtractor);
        PathResult aStarResult = aStar.findPath(sourceNode, destNode);
        System.out.println(aStarResult);
        System.out.println("Path: " + aStarResult.getPathString());

        // Step 6: Compare algorithms
        System.out.println("\n==============================================");
        System.out.println("Algorithm Comparison");
        System.out.println("==============================================");
        compareAlgorithms(dijkstraResult, aStarResult);

        // Cleanup
        hopper.close();
        System.out.println("\n✅ Demo completed successfully!");
    }

    /**
     * Load and configure GraphHopper
     */
    private static GraphHopper loadGraphHopper() {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile("pakistan-251202.osm.pbf");
        hopper.setGraphHopperLocation("graph-cache");
        hopper.setEncodedValuesString("car_access, car_average_speed");
        hopper.setProfiles(
            new Profile("car_profile").setCustomModel(GHUtility.loadCustomModelFromJar("car.json"))
        );
        
        hopper.importOrLoad();
        return hopper;
    }

    /**
     * Compare performance of Dijkstra vs A*
     */
    private static void compareAlgorithms(PathResult dijkstra, PathResult aStar) {
        System.out.println("Metric                  | Dijkstra      | A*            | Winner");
        System.out.println("------------------------|---------------|---------------|----------");
        
        // Path length (should be same or very close)
        System.out.printf("Path distance (km)      | %-13.3f | %-13.3f | %s\n",
            dijkstra.totalDistance / 1000.0,
            aStar.totalDistance / 1000.0,
            Math.abs(dijkstra.totalDistance - aStar.totalDistance) < 1 ? "Same" : 
            (dijkstra.totalDistance < aStar.totalDistance ? "Dijkstra" : "A*"));
        
        // Computation time
        System.out.printf("Compute time (ms)       | %-13.3f | %-13.3f | %s\n",
            dijkstra.computeTimeMs,
            aStar.computeTimeMs,
            dijkstra.computeTimeMs < aStar.computeTimeMs ? "Dijkstra" : "A*");
        
        // Nodes expanded
        System.out.printf("Nodes expanded          | %-13d | %-13d | %s\n",
            dijkstra.nodesExpanded,
            aStar.nodesExpanded,
            dijkstra.nodesExpanded < aStar.nodesExpanded ? "Dijkstra" : "A*");
        
        // Efficiency
        double dijkstraEfficiency = dijkstra.nodesExpanded / dijkstra.computeTimeMs;
        double aStarEfficiency = aStar.nodesExpanded / aStar.computeTimeMs;
        System.out.printf("Nodes/ms (efficiency)   | %-13.1f | %-13.1f | %s\n",
            dijkstraEfficiency,
            aStarEfficiency,
            dijkstraEfficiency > aStarEfficiency ? "Dijkstra" : "A*");
        
        System.out.println("\n📊 Analysis:");
        System.out.println("  - A* typically expands fewer nodes due to heuristic guidance");
        System.out.println("  - Both guarantee optimal path (A* with admissible heuristic)");
        System.out.println("  - A* is preferred for single-source single-destination queries");
    }
}